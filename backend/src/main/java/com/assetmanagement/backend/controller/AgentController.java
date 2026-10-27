package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.dto.AgentReportRequest;
import com.assetmanagement.backend.entity.Device;
import com.assetmanagement.backend.service.AgentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/agent")
@CrossOrigin(originPatterns = "*")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    private final AgentService agentService;
    private final ObjectMapper objectMapper;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    /**
     * Ingestion endpoint for agent telemetry.
     * Supports BOTH single device report object: { "serialNumber": "...", ... }
     * AND multiple device reports in an array: [ { "serialNumber": "...", ... }, ... ]
     */
    @PostMapping("/report")
    public ResponseEntity<?> receiveReport(@RequestBody String rawPayload) {
        long startTime = System.currentTimeMillis();
        log.info("Incoming report request received at: {}", LocalDateTime.now());

        try {
            if (rawPayload == null || rawPayload.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Request body cannot be empty"
                ));
            }

            JsonNode payload = objectMapper.readTree(rawPayload);
            if (payload == null || payload.isNull() || payload.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Request body cannot be empty"
                ));
            }

            if (payload.isArray()) {
                // Multi-device report array
                List<AgentReportRequest> reports = objectMapper.convertValue(
                        payload,
                        new TypeReference<List<AgentReportRequest>>() {}
                );

                log.info("Received batch report with {} device entries", reports.size());
                List<Device> processed = agentService.receiveReports(reports);
                long duration = System.currentTimeMillis() - startTime;

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", String.format("Processed %d of %d device reports successfully in %d ms",
                                processed.size(), reports.size(), duration),
                        "total", reports.size(),
                        "processedCount", processed.size(),
                        "durationMs", duration
                ));
            } else {
                // Single device report object
                AgentReportRequest singleReport = objectMapper.convertValue(
                        payload,
                        AgentReportRequest.class
                );

                log.info("Received single device report for Serial: '{}', Asset Tag: '{}'",
                        singleReport.getSerialNumber(), singleReport.getAssetTag());

                Device device = agentService.receiveReport(singleReport);
                long duration = System.currentTimeMillis() - startTime;

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Agent report received and processed successfully",
                        "deviceId", device.getDeviceId(),
                        "serialNumber", device.getSerialNumber() != null ? device.getSerialNumber() : "",
                        "assetTag", device.getAssetTag() != null ? device.getAssetTag() : "",
                        "durationMs", duration
                ));
            }

        } catch (IllegalArgumentException e) {
            log.warn("Validation error processing agent report: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error processing agent report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Agent report processing error: " + (e.getMessage() != null ? e.getMessage() : "Internal error")
            ));
        }
    }

    public ResponseEntity<?> receiveReport(JsonNode payload) {
        return receiveReport(payload != null ? payload.toString() : "");
    }

    /**
     * Dedicated bulk endpoint for multiple system details at a time.
     */
    @PostMapping(path = {"/reports", "/batch"})
    public ResponseEntity<?> receiveMultipleReports(@RequestBody List<AgentReportRequest> reports) {
        long startTime = System.currentTimeMillis();
        if (reports == null || reports.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Batch report list cannot be empty"
            ));
        }

        log.info("Processing {} system reports via batch endpoint", reports.size());
        try {
            List<Device> processed = agentService.receiveReports(reports);
            long duration = System.currentTimeMillis() - startTime;

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", String.format("Processed %d of %d system reports successfully in %d ms",
                            processed.size(), reports.size(), duration),
                    "total", reports.size(),
                    "processedCount", processed.size(),
                    "durationMs", duration
            ));
        } catch (Exception e) {
            log.error("Batch processing failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Batch processing error: " + e.getMessage()
            ));
        }
    }

    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<Map<String, Object>> handleJsonMappingException(JsonMappingException e) {
        String path = e.getPath().stream()
                .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                .reduce((f, s) -> f + " -> " + s)
                .orElse("unknown");

        String errorMsg = "JSON mapping error at field: " + path + " - " + e.getMessage();
        log.warn(errorMsg);

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", errorMsg
        ));
    }
}
