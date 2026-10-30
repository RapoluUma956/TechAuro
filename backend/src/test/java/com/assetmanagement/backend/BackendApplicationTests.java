package com.assetmanagement.backend;

import com.assetmanagement.backend.controller.AgentController;
import com.assetmanagement.backend.dto.*;
import com.assetmanagement.backend.entity.AuditLog;
import com.assetmanagement.backend.entity.Device;
import com.assetmanagement.backend.entity.DeviceAssignment;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.repository.DeviceAssignmentRepository;
import com.assetmanagement.backend.repository.UserRepository;
import com.assetmanagement.backend.service.UserService;
import java.time.LocalDate;
import com.assetmanagement.backend.entity.HealthThreshold;
import com.assetmanagement.backend.repository.AuditLogRepository;
import com.assetmanagement.backend.repository.DeviceRepository;
import com.assetmanagement.backend.repository.HealthThresholdRepository;
import com.assetmanagement.backend.service.AgentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentController agentController;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private HealthThresholdRepository healthThresholdRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceAssignmentRepository deviceAssignmentRepository;

    @Test
    void testSingleReportUpsert() {
        AgentReportRequest request = new AgentReportRequest();
        request.setSerialNumber("TEST-SERIAL-XYZ");
        request.setAssetTag("TEST-TAG-XYZ");
        request.setBrand("Lenovo");
        request.setModel("ThinkPad T14");

        CpuData cpu = new CpuData();
        cpu.setName("Intel Core i7");
        cpu.setUsagePercent(25.5);
        request.setCpu(cpu);

        Device device = agentService.receiveReport(request);
        assertNotNull(device.getDeviceId());
        assertEquals("TEST-SERIAL-XYZ", device.getSerialNumber());
        assertEquals("ONLINE", device.getConnectionStatus());
        assertEquals("ACTIVE", device.getDeviceStatus());
        assertTrue(device.getAgentInstalled());

        // Update again with new telemetry - same device ID must be maintained
        request.setBrand("Lenovo Updated");
        Device updatedDevice = agentService.receiveReport(request);
        assertEquals(device.getDeviceId(), updatedDevice.getDeviceId());
        assertEquals("Lenovo Updated", updatedDevice.getBrand());
    }

    @Test
    void testMultipleReportsBatch() {
        AgentReportRequest req1 = new AgentReportRequest();
        req1.setSerialNumber("MULTI-DEVICE-001");
        req1.setAssetTag("MULTI-TAG-001");
        req1.setBrand("Dell");
        req1.setModel("Latitude 5420");

        AgentReportRequest req2 = new AgentReportRequest();
        req2.setSerialNumber("MULTI-DEVICE-002");
        req2.setAssetTag("MULTI-TAG-002");
        req2.setBrand("HP");
        req2.setModel("EliteBook 840");

        List<Device> devices = agentService.receiveReports(List.of(req1, req2));
        assertEquals(2, devices.size());
        assertTrue(devices.stream().anyMatch(d -> "MULTI-DEVICE-001".equals(d.getSerialNumber())));
        assertTrue(devices.stream().anyMatch(d -> "MULTI-DEVICE-002".equals(d.getSerialNumber())));
    }

    @Test
    void testDuplicateSoftwareInSingleReport() {
        AgentReportRequest request = new AgentReportRequest();
        request.setSerialNumber("DUP-SW-DEVICE-01");
        request.setAssetTag("DUP-SW-TAG-01");
        request.setBrand("Dell");
        request.setModel("XPS 15");

        SoftwareData sw1 = new SoftwareData();
        sw1.setSoftwareName("Google Chrome");
        sw1.setVersion("120.0.0.1");

        // Duplicate software name (e.g. 32-bit & 64-bit or winget + registry duplicate)
        SoftwareData sw2 = new SoftwareData();
        sw2.setSoftwareName("Google Chrome");
        sw2.setVersion("120.0.0.2");

        request.setInstalledSoftware(List.of(sw1, sw2));

        // Must NOT throw unique constraint violation
        Device device = agentService.receiveReport(request);
        assertNotNull(device.getDeviceId());
    }

    @Test
    void testControllerEndpointWithSingleAndArrayPayload() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Single object payload to /report
        String singleJson = "{\"serialNumber\":\"CTRL-001\",\"assetTag\":\"CTRL-TAG-001\",\"brand\":\"Lenovo\",\"model\":\"Yoga\"}";
        ResponseEntity<?> singleResp = agentController.receiveReport(singleJson);
        assertEquals(200, singleResp.getStatusCode().value());

        // Array payload to /report
        String arrayJson = "[{\"serialNumber\":\"CTRL-002\",\"assetTag\":\"CTRL-TAG-002\",\"brand\":\"Dell\",\"model\":\"OptiPlex\"}," +
                "{\"serialNumber\":\"CTRL-003\",\"assetTag\":\"CTRL-TAG-003\",\"brand\":\"HP\",\"model\":\"ProDesk\"}]";
        ResponseEntity<?> arrayResp = agentController.receiveReport(arrayJson);
        assertEquals(200, arrayResp.getStatusCode().value());
    }

    @Test
    void testRealCollectedReportIfPresent() throws Exception {
        File file = new File("c:/Users/SathwikaKalvakuntla/OneDrive - eWorld Enterprise Solutions, Inc/Documents/assertmanagement/assert-montoring-agent/collected_report.json");
        if (file.exists()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            AgentReportRequest request = mapper.readValue(file, AgentReportRequest.class);
            Device device = agentService.receiveReport(request);
            assertNotNull(device.getDeviceId());
            assertEquals("ONLINE", device.getConnectionStatus());
        }
    }

    @Test
    void testHealthThresholds() {
        Optional<HealthThreshold> currentOpt = healthThresholdRepository.findCurrentThresholds();
        assertTrue(currentOpt.isPresent(), "Default health threshold should exist");
        HealthThreshold threshold = currentOpt.get();
        if (threshold.getCpuWarningPercent() != 90) {
            threshold.setCpuWarningPercent(90);
            threshold = healthThresholdRepository.save(threshold);
        }
        assertNotNull(threshold.getThresholdId());
        assertEquals(90, threshold.getCpuWarningPercent());
        assertEquals(5, threshold.getOfflineTimeoutMinutes());

        try {
            // Update threshold
            threshold.setCpuWarningPercent(85);
            HealthThreshold updated = healthThresholdRepository.save(threshold);
            assertEquals(85, updated.getCpuWarningPercent());
            assertNotNull(updated.getUpdatedAt());
        } finally {
            // Reset threshold back to 90 so subsequent test runs remain idempotent
            threshold.setCpuWarningPercent(90);
            healthThresholdRepository.save(threshold);
        }
    }

    @Test
    void testAuditLogs() {
        AuditLog log = new AuditLog();
        log.setAction("UPDATE_DEVICE");
        log.setEntityType("DEVICE");
        log.setEntityId(1L);
        log.setOldValue("{\"deviceStatus\":\"OFFLINE\"}");
        log.setNewValue("{\"deviceStatus\":\"ONLINE\"}");
        log.setIpAddress("127.0.0.1");

        AuditLog saved = auditLogRepository.save(log);
        assertNotNull(saved.getAuditId());
        assertNotNull(saved.getCreatedAt());
        assertEquals("UPDATE_DEVICE", saved.getAction());
        assertEquals("DEVICE", saved.getEntityType());

        List<AuditLog> found = auditLogRepository.findByEntityTypeAndEntityId("DEVICE", 1L);
        assertFalse(found.isEmpty());
        assertTrue(found.stream().anyMatch(a -> a.getAuditId().equals(saved.getAuditId())));
    }

    @Test
    void testUserCreationAndDeviceAssignmentBySerialNumber() {
        String serial = "SN-AGENT-TEST-001";
        String empCode = "EMP-TEST-" + System.currentTimeMillis();
        String email = "test." + System.currentTimeMillis() + "@techaura.io";

        // 1. Ensure test device exists
        Device dev = deviceRepository.findBySerialNumberIgnoreCase(serial).orElseGet(() -> {
            Device d = new Device();
            d.setSerialNumber(serial);
            d.setAssetTag("AST-" + serial);
            d.setBrand("Lenovo");
            d.setModel("ThinkPad P16");
            d.setConnectionStatus("ONLINE");
            d.setDeviceStatus("ACTIVE");
            return deviceRepository.save(d);
        });

        // 2. Create User with device assigned by Serial Number
        UserRequest userRequest = new UserRequest(
                empCode,
                "Sathwika",
                "Engineer",
                email,
                "+1 555-0199",
                "Password@123",
                "Senior Systems Engineer",
                LocalDate.now(),
                serial,
                null
        );

        UserResponse userResponse = userService.createUser(userRequest);
        assertNotNull(userResponse.userId());
        assertEquals(empCode, userResponse.employeeCode());
        assertEquals(serial, userResponse.assignedSerialNumber());
        assertEquals("ONLINE", userResponse.assignedConnectionStatus());

        // 3. Verify active assignment in DB
        Optional<DeviceAssignment> activeAssignment = deviceAssignmentRepository
                .findByUser_UserIdAndStatus(userResponse.userId(), "ACTIVE");
        assertTrue(activeAssignment.isPresent());
        assertEquals(serial, activeAssignment.get().getDevice().getSerialNumber());
        assertEquals(serial, activeAssignment.get().getSerialNumber());

        // Verify device current_user_id updated
        Device updatedDev = deviceRepository.findById(dev.getDeviceId()).orElseThrow();
        assertEquals(userResponse.userId(), updatedDev.getCurrentUserId());

        // 4. Retrieve complete Employee Detail and verify telemetry mapping
        EmployeeDetailResponse detail = userService.getEmployeeDetail(userResponse.userId());
        assertNotNull(detail);
        assertEquals(userResponse.userId(), detail.userId());
        assertNotNull(detail.device());
        assertEquals(serial, detail.device().getSerialNumber());
        assertNotNull(detail.currentAssignment());
        assertEquals("ACTIVE", detail.currentAssignment().status());

        // 5. Test unassigning device
        UserResponse unassignedResponse = userService.unassignDevice(userResponse.userId());
        assertNull(unassignedResponse.assignedSerialNumber());

        Device devAfterUnassign = deviceRepository.findById(dev.getDeviceId()).orElseThrow();
        assertNull(devAfterUnassign.getCurrentUserId());

        Optional<DeviceAssignment> afterUnassign = deviceAssignmentRepository
                .findByUser_UserIdAndStatus(userResponse.userId(), "ACTIVE");
        assertTrue(afterUnassign.isEmpty());

        // 6. Test reassigning device by serial number
        UserResponse reassignedResponse = userService.assignDevice(
                userResponse.userId(),
                new AssignDeviceRequest(serial, null)
        );
        assertEquals(serial, reassignedResponse.assignedSerialNumber());

        // Clean up
        deviceAssignmentRepository.deleteAll(deviceAssignmentRepository.findByDevice_DeviceId(dev.getDeviceId()));
        userService.deleteUser(userResponse.userId());
        deviceRepository.delete(dev);
    }
}
