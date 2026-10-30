package com.assetmanagement.backend.service;

import com.assetmanagement.backend.dto.accessory.*;
import com.assetmanagement.backend.entity.Accessory;
import com.assetmanagement.backend.entity.AccessoryAssignment;
import com.assetmanagement.backend.entity.Device;
import com.assetmanagement.backend.entity.DeviceAssignment;
import com.assetmanagement.backend.repository.AccessoryAssignmentRepository;
import com.assetmanagement.backend.repository.AccessoryRepository;
import com.assetmanagement.backend.repository.DeviceAssignmentRepository;
import com.assetmanagement.backend.repository.DeviceRepository;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccessoryService {

    private final AccessoryRepository accessoryRepository;
    private final AccessoryAssignmentRepository accessoryAssignmentRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceAssignmentRepository deviceAssignmentRepository;
    private final UserRepository userRepository;

    public AccessoryService(
            AccessoryRepository accessoryRepository,
            AccessoryAssignmentRepository accessoryAssignmentRepository,
            DeviceRepository deviceRepository,
            DeviceAssignmentRepository deviceAssignmentRepository,
            UserRepository userRepository) {
        this.accessoryRepository = accessoryRepository;
        this.accessoryAssignmentRepository = accessoryAssignmentRepository;
        this.deviceRepository = deviceRepository;
        this.deviceAssignmentRepository = deviceAssignmentRepository;
        this.userRepository = userRepository;
    }

    // ==================== DASHBOARD SUMMARY ====================

    @Transactional(readOnly = true)
    public AccessorySummaryResponse getSummary() {
        List<Accessory> accessories = accessoryRepository.findAll();
        List<Device> devices = deviceRepository.findAll();
        List<AccessoryAssignment> activeAccessoryAssignments = accessoryAssignmentRepository.findByStatus("ACTIVE");
        List<DeviceAssignment> activeDeviceAssignments = deviceAssignmentRepository.findByStatus("ACTIVE");

        // 1. Accessory counts
        long accessoryTotalUnits = accessories.stream()
                .mapToLong(a -> a.getTotalQuantity() != null ? a.getTotalQuantity() : 0)
                .sum();

        // Calculate assigned accessory units:
        long accessoryAssignedUnits = 0;
        if (!activeAccessoryAssignments.isEmpty()) {
            accessoryAssignedUnits = activeAccessoryAssignments.stream()
                    .mapToLong(aa -> aa.getQuantity() != null ? aa.getQuantity() : 1)
                    .sum();
        } else {
            // fallback if assignments not yet tracked individually
            accessoryAssignedUnits = accessories.stream()
                    .mapToLong(a -> {
                        int tot = a.getTotalQuantity() != null ? a.getTotalQuantity() : 0;
                        int avail = a.getAvailableQuantity() != null ? a.getAvailableQuantity() : 0;
                        int dam = a.getDamagedQuantity() != null ? a.getDamagedQuantity() : 0;
                        return Math.max(0, tot - avail - dam);
                    })
                    .sum();
        }

        long accessoryAvailableUnits = accessories.stream()
                .mapToLong(a -> a.getAvailableQuantity() != null ? a.getAvailableQuantity() : 0)
                .sum();

        long accessoryDamagedUnits = accessories.stream()
                .mapToLong(a -> a.getDamagedQuantity() != null ? a.getDamagedQuantity() : 0)
                .sum();

        // 2. Device (Laptop) counts
        long deviceTotal = devices.size();

        Set<Long> assignedDeviceIds = activeDeviceAssignments.stream()
                .filter(da -> da.getDevice() != null)
                .map(da -> da.getDevice().getDeviceId())
                .collect(Collectors.toSet());

        long deviceAssigned = devices.stream()
                .filter(d -> assignedDeviceIds.contains(d.getDeviceId()) || d.getCurrentUserId() != null)
                .count();

        long deviceRepair = devices.stream()
                .filter(d -> {
                    String st = (d.getStatus() != null ? d.getStatus() : "") + " " +
                                (d.getDeviceStatus() != null ? d.getDeviceStatus() : "") + " " +
                                (d.getAssetStatus() != null ? d.getAssetStatus() : "");
                    st = st.toUpperCase();
                    return st.contains("REPAIR") || st.contains("MAINTENANCE") || st.contains("DAMAGED");
                })
                .count();

        long deviceUnassigned = Math.max(0, deviceTotal - deviceAssigned - deviceRepair);

        // Combined totals matching specification
        long totalAll = accessoryTotalUnits + deviceTotal;
        long assignedAll = accessoryAssignedUnits + deviceAssigned;
        long unassignedAll = accessoryAvailableUnits + deviceUnassigned;
        long repairAll = accessoryDamagedUnits + deviceRepair;

        return new AccessorySummaryResponse(totalAll, assignedAll, unassignedAll, repairAll);
    }

    // ==================== ACCESSORIES TABLE WITH PAGINATION & SEARCH ====================

    @Transactional(readOnly = true)
    public AccessoryPageResponse getAccessoriesPage(int page, int size, String search, String category) {
        List<Accessory> all = accessoryRepository.findAll();
        List<AccessoryAssignment> activeAssignments = accessoryAssignmentRepository.findByStatus("ACTIVE");

        Map<Long, Integer> assignedMap = activeAssignments.stream()
                .filter(aa -> aa.getAccessory() != null)
                .collect(Collectors.groupingBy(
                        aa -> aa.getAccessory().getAccessoryId(),
                        Collectors.summingInt(aa -> aa.getQuantity() != null ? aa.getQuantity() : 1)
                ));

        String query = (search != null) ? search.trim().toLowerCase() : "";
        String catFilter = (category != null) ? category.trim().toLowerCase() : "";

        List<AccessoryItemDto> items = all.stream()
                .filter(a -> {
                    if (!catFilter.isEmpty() && !catFilter.equals("all")) {
                        String itemCat = (a.getCategory() != null ? a.getCategory() : "").toLowerCase();
                        if (!itemCat.equals(catFilter)) {
                            return false;
                        }
                    }
                    if (!query.isEmpty()) {
                        String name = (a.getAccessoryName() != null ? a.getAccessoryName() : "").toLowerCase();
                        String prod = (a.getProductName() != null ? a.getProductName() : "").toLowerCase();
                        String brand = (a.getBrand() != null ? a.getBrand() : "").toLowerCase();
                        String model = (a.getModel() != null ? a.getModel() : "").toLowerCase();
                        String cat = (a.getCategory() != null ? a.getCategory() : "").toLowerCase();
                        return name.contains(query) || prod.contains(query) || brand.contains(query) ||
                                model.contains(query) || cat.contains(query);
                    }
                    return true;
                })
                .map(a -> {
                    AccessoryItemDto dto = new AccessoryItemDto();
                    dto.setAccessoryId(a.getAccessoryId());
                    dto.setAccessoryName(a.getAccessoryName());
                    dto.setCategory(a.getCategory());
                    dto.setSubCategory(a.getSubCategory());
                    dto.setBrand(a.getBrand());
                    dto.setManufacturer(a.getManufacturer());
                    dto.setModel(a.getModel());
                    dto.setDescription(a.getDescription());
                    dto.setTotalQuantity(a.getTotalQuantity() != null ? a.getTotalQuantity() : 0);

                    int assigned = assignedMap.getOrDefault(a.getAccessoryId(), 0);
                    if (assigned == 0 && a.getTotalQuantity() != null) {
                        int tot = a.getTotalQuantity();
                        int avail = a.getAvailableQuantity() != null ? a.getAvailableQuantity() : 0;
                        int dam = a.getDamagedQuantity() != null ? a.getDamagedQuantity() : 0;
                        assigned = Math.max(0, tot - avail - dam);
                    }
                    dto.setAssignedQuantity(assigned);

                    dto.setAvailableQuantity(a.getAvailableQuantity() != null ? a.getAvailableQuantity() : 0);
                    dto.setDamagedQuantity(a.getDamagedQuantity() != null ? a.getDamagedQuantity() : 0);
                    dto.setRepairQuantity(a.getDamagedQuantity() != null ? a.getDamagedQuantity() : 0);
                    dto.setPurchaseDate(a.getPurchaseDate());
                    dto.setSerialNumber(a.getSerialNumber());
                    return dto;
                })
                .sorted(Comparator.comparing(AccessoryItemDto::getAccessoryId))
                .collect(Collectors.toList());

        int totalElements = items.size();
        int safeSize = (size <= 0) ? 10 : size;
        int safePage = Math.max(0, page);
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        if (totalPages == 0) totalPages = 1;

        int fromIndex = Math.min(safePage * safeSize, totalElements);
        int toIndex = Math.min(fromIndex + safeSize, totalElements);
        List<AccessoryItemDto> pagedContent = items.subList(fromIndex, toIndex);

        return new AccessoryPageResponse(pagedContent, safePage, safeSize, totalElements, totalPages);
    }

    // ==================== CREATE / ADD ACCESSORY (MERGE OR NEW) ====================

    @Transactional
    public CreateAccessoryResponse createOrAddAccessory(CreateAccessoryRequest req) {
        if (req.getAccessoryName() == null || req.getAccessoryName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Accessory name is required");
        }
        if (req.getCategory() == null || req.getCategory().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is required");
        }
        if (req.getBrand() == null || req.getBrand().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand is required");
        }

        int qtyToAdd = (req.getQuantity() != null && req.getQuantity() > 0) ? req.getQuantity() : 1;

        // Check if an accessory of same category, brand, and model already exists
        Optional<Accessory> existingOpt = Optional.empty();
        if (req.getModel() != null && !req.getModel().isBlank()) {
            existingOpt = accessoryRepository.findFirstByCategoryIgnoreCaseAndBrandIgnoreCaseAndModelIgnoreCase(
                    req.getCategory().trim(), req.getBrand().trim(), req.getModel().trim());
            if (existingOpt.isEmpty()) {
                existingOpt = accessoryRepository.findFirstByAccessoryNameIgnoreCaseAndBrandIgnoreCaseAndModelIgnoreCase(
                        req.getAccessoryName().trim(), req.getBrand().trim(), req.getModel().trim());
            }
        }

        Accessory saved;
        if (existingOpt.isPresent()) {
            Accessory existing = existingOpt.get();
            int currentTotal = existing.getTotalQuantity() != null ? existing.getTotalQuantity() : 0;
            int currentAvail = existing.getAvailableQuantity() != null ? existing.getAvailableQuantity() : 0;
            existing.setTotalQuantity(currentTotal + qtyToAdd);
            existing.setAvailableQuantity(currentAvail + qtyToAdd);
            if (req.getDescription() != null && !req.getDescription().isBlank()) {
                existing.setDescription(req.getDescription());
            }
            if (req.getSubCategory() != null && !req.getSubCategory().isBlank()) {
                existing.setSubCategory(req.getSubCategory());
            }
            saved = accessoryRepository.save(existing);
        } else {
            Accessory a = new Accessory();
            a.setAccessoryName(req.getAccessoryName().trim());
            a.setProductName(req.getAccessoryName().trim());
            a.setCategory(req.getCategory().trim());
            a.setSubCategory(req.getSubCategory());
            a.setBrand(req.getBrand().trim());
            a.setManufacturer(req.getManufacturer() != null && !req.getManufacturer().isBlank() ? req.getManufacturer().trim() : req.getBrand().trim());
            a.setModel(req.getModel());
            a.setDescription(req.getDescription());
            a.setTotalQuantity(qtyToAdd);
            a.setAvailableQuantity(qtyToAdd);
            a.setDamagedQuantity(0);
            a.setUnavailableQuantity(0);
            a.setPurchaseDate(req.getPurchaseDate() != null ? req.getPurchaseDate() : LocalDate.now());
            a.setSpecifications(req.getSpecifications());
            saved = accessoryRepository.save(a);
        }

        return new CreateAccessoryResponse(
                "Accessory inventory added successfully",
                saved.getAccessoryId(),
                saved.getAccessoryName(),
                qtyToAdd,
                saved.getTotalQuantity(),
                saved.getAvailableQuantity()
        );
    }

    // ==================== LAPTOP CONFIGURATIONS & BULK REGISTRATION ====================

    @Transactional(readOnly = true)
    public LaptopSummaryResponse getLaptopSummary() {
        List<Device> devices = deviceRepository.findAll();
        List<DeviceAssignment> activeAssignments = deviceAssignmentRepository.findByStatus("ACTIVE");

        Set<Long> assignedDeviceIds = activeAssignments.stream()
                .filter(da -> da.getDevice() != null)
                .map(da -> da.getDevice().getDeviceId())
                .collect(Collectors.toSet());

        long total = devices.size();
        long assigned = devices.stream()
                .filter(d -> assignedDeviceIds.contains(d.getDeviceId()) || d.getCurrentUserId() != null)
                .count();

        long repair = devices.stream()
                .filter(d -> {
                    String st = (d.getStatus() != null ? d.getStatus() : "") + " " +
                                (d.getDeviceStatus() != null ? d.getDeviceStatus() : "") + " " +
                                (d.getAssetStatus() != null ? d.getAssetStatus() : "");
                    st = st.toUpperCase();
                    return st.contains("REPAIR") || st.contains("MAINTENANCE") || st.contains("DAMAGED");
                })
                .count();

        long available = Math.max(0, total - assigned - repair);

        return new LaptopSummaryResponse(total, assigned, available, repair);
    }

    @Transactional(readOnly = true)
    public LaptopConfigurationPageResponse getLaptopConfigurations(
            String brand, String ram, String ssd, String search, int page, int size) {

        List<Device> allDevices = deviceRepository.findAll();
        List<DeviceAssignment> activeAssignments = deviceAssignmentRepository.findByStatus("ACTIVE");

        Map<Long, DeviceAssignment> assignmentMap = new HashMap<>();
        for (DeviceAssignment da : activeAssignments) {
            if (da.getDevice() != null) {
                assignmentMap.put(da.getDevice().getDeviceId(), da);
            }
        }

        // Group devices by (brand, model, ram, ssd)
        Map<String, List<Device>> groups = new LinkedHashMap<>();

        for (Device d : allDevices) {
            String b = (d.getBrand() != null && !d.getBrand().isBlank()) ? d.getBrand().trim() : "Unknown";
            String m = (d.getModel() != null && !d.getModel().isBlank()) ? d.getModel().trim() : "Standard";
            String r = formatRam(d);
            String s = formatSsd(d);

            String key = b.toLowerCase() + "|||" + m.toLowerCase() + "|||" + r.toLowerCase() + "|||" + s.toLowerCase();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(d);
        }

        String searchLower = (search != null) ? search.trim().toLowerCase() : "";
        String brandFilter = (brand != null) ? brand.trim().toLowerCase() : "";
        String ramFilter = (ram != null) ? ram.trim().toLowerCase() : "";
        String ssdFilter = (ssd != null) ? ssd.trim().toLowerCase() : "";

        List<LaptopConfigurationDto> configs = new ArrayList<>();

        for (Map.Entry<String, List<Device>> entry : groups.entrySet()) {
            List<Device> devList = entry.getValue();
            if (devList.isEmpty()) continue;

            Device first = devList.get(0);
            String b = (first.getBrand() != null) ? first.getBrand().trim() : "Unknown";
            String m = (first.getModel() != null) ? first.getModel().trim() : "Standard";
            String r = formatRam(first);
            String s = formatSsd(first);

            // Filter checks
            if (!brandFilter.isEmpty() && !brandFilter.equals("all") && !b.toLowerCase().equals(brandFilter)) {
                continue;
            }
            if (!ramFilter.isEmpty() && !ramFilter.equals("all") && !r.toLowerCase().equals(ramFilter)) {
                continue;
            }
            if (!ssdFilter.isEmpty() && !ssdFilter.equals("all") && !s.toLowerCase().equals(ssdFilter)) {
                continue;
            }
            if (!searchLower.isEmpty()) {
                boolean match = b.toLowerCase().contains(searchLower) ||
                                m.toLowerCase().contains(searchLower) ||
                                r.toLowerCase().contains(searchLower) ||
                                s.toLowerCase().contains(searchLower);
                if (!match) continue;
            }

            long total = devList.size();
            long assigned = 0;
            long repair = 0;

            List<LaptopDeviceItemDto> deviceDtos = new ArrayList<>();

            for (Device d : devList) {
                LaptopDeviceItemDto item = new LaptopDeviceItemDto();
                item.setDeviceId(d.getDeviceId());
                item.setAssetTag(d.getAssetTag());
                item.setSerialNumber(d.getSerialNumber());
                item.setBrand(d.getBrand());
                item.setModel(d.getModel());
                item.setRam(formatRam(d));
                item.setSsd(formatSsd(d));
                item.setWarrantyExpiryDate(d.getWarrantyExpiryDate());
                item.setPurchaseDate(d.getPurchaseDate());

                DeviceAssignment da = assignmentMap.get(d.getDeviceId());
                boolean isAssigned = (da != null) || (d.getCurrentUserId() != null);
                if (isAssigned) {
                    assigned++;
                    item.setAssignmentStatus("ASSIGNED");
                    if (da != null && da.getUser() != null) {
                        item.setAssignedUserName(da.getUser().getFirstName() + " " + (da.getUser().getLastName() != null ? da.getUser().getLastName() : ""));
                    }
                } else {
                    item.setAssignmentStatus("UNASSIGNED");
                }

                String st = (d.getStatus() != null ? d.getStatus() : "") + " " +
                            (d.getDeviceStatus() != null ? d.getDeviceStatus() : "") + " " +
                            (d.getAssetStatus() != null ? d.getAssetStatus() : "");
                st = st.toUpperCase();
                boolean isRepair = st.contains("REPAIR") || st.contains("MAINTENANCE") || st.contains("DAMAGED");
                if (isRepair) {
                    repair++;
                    item.setDeviceStatus("REPAIR");
                } else {
                    item.setDeviceStatus(d.getDeviceStatus() != null ? d.getDeviceStatus() : "ACTIVE");
                }

                deviceDtos.add(item);
            }

            long remaining = Math.max(0, total - assigned - repair);

            LaptopConfigurationDto configDto = new LaptopConfigurationDto(b, m, r, s, total, assigned, remaining, repair);
            configDto.setDevices(deviceDtos);
            configs.add(configDto);
        }

        int totalElements = configs.size();
        int safeSize = (size <= 0) ? 10 : size;
        int safePage = Math.max(0, page);
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        if (totalPages == 0) totalPages = 1;

        int fromIndex = Math.min(safePage * safeSize, totalElements);
        int toIndex = Math.min(fromIndex + safeSize, totalElements);
        List<LaptopConfigurationDto> pagedContent = configs.subList(fromIndex, toIndex);

        return new LaptopConfigurationPageResponse(pagedContent, safePage, safeSize, totalElements, totalPages);
    }

    @Transactional
    public BulkAddLaptopResponse bulkAddLaptops(BulkAddLaptopRequest req) {
        if (req.getBrand() == null || req.getBrand().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand is required");
        }
        if (req.getModel() == null || req.getModel().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model is required");
        }
        if (req.getQuantity() == null || req.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }
        if (req.getSerialNumbers() == null || req.getSerialNumbers().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serial numbers list is required");
        }
        if (req.getSerialNumbers().size() != req.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Number of serial numbers (" + req.getSerialNumbers().size() + ") must match quantity (" + req.getQuantity() + ")");
        }

        // Validate duplicates within the request
        Set<String> seen = new HashSet<>();
        for (String sn : req.getSerialNumbers()) {
            String trimmed = sn.trim();
            if (trimmed.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serial numbers cannot be blank");
            }
            if (!seen.add(trimmed.toUpperCase())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate serial number in request: " + trimmed);
            }
        }

        // Check if any serial number already exists in database
        for (String sn : req.getSerialNumbers()) {
            if (deviceRepository.existsBySerialNumber(sn.trim())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Serial number already exists: " + sn.trim());
            }
        }

        BigDecimal ramGb = parseRamGb(req.getRam());
        BigDecimal ssdGb = parseSsdGb(req.getSsd());

        List<Device> savedDevices = new ArrayList<>();
        List<LaptopDeviceItemDto> deviceDtos = new ArrayList<>();

        LocalDate purchase = (req.getPurchaseDate() != null) ? req.getPurchaseDate() : LocalDate.now();

        for (String rawSn : req.getSerialNumbers()) {
            String sn = rawSn.trim();

            Device d = new Device();
            d.setBrand(req.getBrand().trim());
            d.setModel(req.getModel().trim());
            d.setDeviceName(req.getBrand().trim() + " " + req.getModel().trim());
            d.setSerialNumber(sn);

            // Generate clean unique asset tag
            String prefix = req.getBrand().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
            if (prefix.length() > 4) prefix = prefix.substring(0, 4);
            if (prefix.isEmpty()) prefix = "DEV";
            String assetTag = "AST-" + prefix + "-" + sn;
            if (deviceRepository.existsByAssetTag(assetTag)) {
                assetTag = "AST-" + prefix + "-" + System.currentTimeMillis() % 100000 + "-" + sn;
            }
            d.setAssetTag(assetTag);

            d.setRamTotalGb(ramGb);
            d.setRamGb(ramGb);
            d.setStorageTotalGb(ssdGb);
            d.setSsdCapacityGb(ssdGb);
            d.setStorageType("SSD");
            d.setSsdType("NVMe M.2");

            d.setWarrantyStartDate(purchase);
            d.setWarrantyEndDate(req.getWarrantyExpiryDate());
            d.setWarrantyExpiryDate(req.getWarrantyExpiryDate());
            d.setPurchaseDate(purchase);

            d.setDeviceStatus("ACTIVE");
            d.setStatus("ACTIVE");
            d.setConnectionStatus("OFFLINE");
            d.setAgentInstalled(false);

            Device saved = deviceRepository.save(d);
            savedDevices.add(saved);

            LaptopDeviceItemDto item = new LaptopDeviceItemDto();
            item.setDeviceId(saved.getDeviceId());
            item.setAssetTag(saved.getAssetTag());
            item.setSerialNumber(saved.getSerialNumber());
            item.setBrand(saved.getBrand());
            item.setModel(saved.getModel());
            item.setRam(formatRam(saved));
            item.setSsd(formatSsd(saved));
            item.setWarrantyExpiryDate(saved.getWarrantyExpiryDate());
            item.setPurchaseDate(saved.getPurchaseDate());
            item.setAssignmentStatus("UNASSIGNED");
            item.setDeviceStatus("ACTIVE");
            deviceDtos.add(item);
        }

        return new BulkAddLaptopResponse(
                "Laptops registered successfully",
                savedDevices.size(),
                deviceDtos
        );
    }

    @Transactional(readOnly = true)
    public LaptopDeviceItemDto getLaptopById(Long deviceId) {
        Device d = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laptop not found with id: " + deviceId));

        LaptopDeviceItemDto item = new LaptopDeviceItemDto();
        item.setDeviceId(d.getDeviceId());
        item.setAssetTag(d.getAssetTag());
        item.setSerialNumber(d.getSerialNumber());
        item.setBrand(d.getBrand());
        item.setModel(d.getModel());
        item.setRam(formatRam(d));
        item.setSsd(formatSsd(d));
        item.setWarrantyExpiryDate(d.getWarrantyExpiryDate());
        item.setPurchaseDate(d.getPurchaseDate());

        Optional<DeviceAssignment> daOpt = deviceAssignmentRepository.findByDevice_DeviceIdAndStatus(deviceId, "ACTIVE");
        if (daOpt.isPresent()) {
            item.setAssignmentStatus("ASSIGNED");
            if (daOpt.get().getUser() != null) {
                item.setAssignedUserName(daOpt.get().getUser().getFirstName() + " " +
                        (daOpt.get().getUser().getLastName() != null ? daOpt.get().getUser().getLastName() : ""));
            }
        } else {
            item.setAssignmentStatus("UNASSIGNED");
        }

        item.setDeviceStatus(d.getDeviceStatus() != null ? d.getDeviceStatus() : "ACTIVE");
        return item;
    }

    // ==================== CATEGORIES DROPDOWN ====================

    public List<CategoryOptionDto> getCategories() {
        return List.of(
                new CategoryOptionDto("Mouse", List.of("Wireless", "Wired", "Bluetooth", "Ergonomic")),
                new CategoryOptionDto("Keyboard", List.of("Mechanical", "Wireless", "Wired", "Ergonomic")),
                new CategoryOptionDto("Monitor", List.of("Standard 24\"", "Standard 27\"", "Ultrawide", "4K UHD")),
                new CategoryOptionDto("Headset", List.of("Noise-Canceling", "USB Wired", "Bluetooth", "3.5mm")),
                new CategoryOptionDto("Docking Station", List.of("Thunderbolt 4", "USB-C Dual 4K", "DisplayLink")),
                new CategoryOptionDto("Charger / Adapter", List.of("USB-C 65W", "USB-C 100W", "Multi-Port GaN")),
                new CategoryOptionDto("Webcam", List.of("1080p Full HD", "4K Ultra HD", "Privacy Shutter")),
                new CategoryOptionDto("Other", List.of("Cables", "Laptop Stand", "Sleeve / Bag"))
        );
    }

    // ==================== CRUD ACCESSORIES ====================

    @Transactional(readOnly = true)
    public List<Accessory> getAllAccessories() {
        return accessoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Accessory getAccessoryById(Long id) {
        return accessoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Accessory not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Accessory> getByCategory(String category) {
        return accessoryRepository.findByCategory(category);
    }

    @Transactional
    public Accessory createAccessory(Accessory accessory) {
        return accessoryRepository.save(accessory);
    }

    @Transactional
    public Accessory updateAccessory(Long id, Accessory updated) {
        Accessory existing = getAccessoryById(id);
        if (updated.getCategory() != null) existing.setCategory(updated.getCategory());
        if (updated.getSubCategory() != null) existing.setSubCategory(updated.getSubCategory());
        if (updated.getBrand() != null) existing.setBrand(updated.getBrand());
        if (updated.getManufacturer() != null) existing.setManufacturer(updated.getManufacturer());
        if (updated.getModel() != null) existing.setModel(updated.getModel());
        if (updated.getProductName() != null) existing.setProductName(updated.getProductName());
        if (updated.getAccessoryName() != null) existing.setAccessoryName(updated.getAccessoryName());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getSpecifications() != null) existing.setSpecifications(updated.getSpecifications());
        if (updated.getTotalQuantity() != null) existing.setTotalQuantity(updated.getTotalQuantity());
        if (updated.getAvailableQuantity() != null) existing.setAvailableQuantity(updated.getAvailableQuantity());
        if (updated.getDamagedQuantity() != null) existing.setDamagedQuantity(updated.getDamagedQuantity());
        if (updated.getPurchaseDate() != null) existing.setPurchaseDate(updated.getPurchaseDate());
        if (updated.getSerialNumber() != null) existing.setSerialNumber(updated.getSerialNumber());
        if (updated.getUnavailableQuantity() != null) existing.setUnavailableQuantity(updated.getUnavailableQuantity());
        return accessoryRepository.save(existing);
    }

    @Transactional
    public void deleteAccessory(Long id) {
        Accessory existing = getAccessoryById(id);
        accessoryRepository.delete(existing);
    }

    // ==================== ASSIGNMENT OPERATIONS ====================

    @Transactional
    public AssignAccessoryResponse assignAccessory(Long accessoryId, AssignAccessoryRequest req) {
        Accessory acc = getAccessoryById(accessoryId);
        int qty = (req.getQuantity() != null && req.getQuantity() > 0) ? req.getQuantity() : 1;

        int avail = acc.getAvailableQuantity() != null ? acc.getAvailableQuantity() : 0;
        if (avail < qty) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough available units to assign. Available: " + avail);
        }

        if (req.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee ID is required");
        }

        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee with ID " + req.getUserId() + " not found"));

        AccessoryAssignment assignment = new AccessoryAssignment();
        assignment.setAccessory(acc);
        assignment.setUser(user);
        assignment.setQuantity(qty);
        assignment.setStatus("ACTIVE");
        if (req.getAssignedDate() != null) {
            assignment.setAssignedAt(req.getAssignedDate().atStartOfDay());
        } else {
            assignment.setAssignedAt(LocalDateTime.now());
        }
        AccessoryAssignment savedAssignment = accessoryAssignmentRepository.save(assignment);

        acc.setAvailableQuantity(avail - qty);
        accessoryRepository.save(acc);

        String employeeName = (user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "")).trim();

        return new AssignAccessoryResponse(
                "Accessory assigned successfully",
                savedAssignment.getAssignmentId(),
                acc.getAccessoryId(),
                acc.getAccessoryName(),
                user.getUserId(),
                employeeName,
                qty,
                acc.getAvailableQuantity()
        );
    }

    @Transactional
    public AssignAccessoryResponse assignLaptop(Long deviceId, AssignAccessoryRequest req) {
        Device dev = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laptop device not found with ID " + deviceId));

        if (req.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee ID is required");
        }

        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found with ID " + req.getUserId()));

        // Close any existing active assignment for this device
        Optional<DeviceAssignment> existingActive = deviceAssignmentRepository.findByDevice_DeviceIdAndStatus(deviceId, "ACTIVE");
        existingActive.ifPresent(a -> {
            a.setStatus("RETURNED");
            a.setReturnedAt(LocalDateTime.now());
            deviceAssignmentRepository.save(a);
        });

        DeviceAssignment da = new DeviceAssignment();
        da.setDevice(dev);
        da.setUser(user);
        da.setSerialNumber(dev.getSerialNumber());
        da.setStatus("ACTIVE");
        if (req.getAssignedDate() != null) {
            da.setAssignedAt(req.getAssignedDate().atStartOfDay());
        } else {
            da.setAssignedAt(LocalDateTime.now());
        }
        DeviceAssignment savedDa = deviceAssignmentRepository.save(da);

        dev.setCurrentUserId(user.getUserId());
        dev.setStatus("ACTIVE");
        deviceRepository.save(dev);

        String employeeName = (user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "")).trim();
        String devName = dev.getDeviceName() != null ? dev.getDeviceName() : (dev.getBrand() + " " + dev.getModel());

        return new AssignAccessoryResponse(
                "Laptop device assigned successfully",
                savedDa.getAssignmentId(),
                dev.getDeviceId(),
                devName,
                user.getUserId(),
                employeeName,
                1,
                0
        );
    }

    // ==================== HELPER METHODS ====================

    private String formatRam(Device d) {
        BigDecimal ram = d.getRamTotalGb() != null ? d.getRamTotalGb() : d.getRamGb();
        if (ram != null && ram.compareTo(BigDecimal.ZERO) > 0) {
            return ram.intValue() + " GB";
        }
        return "16 GB";
    }

    private String formatSsd(Device d) {
        BigDecimal cap = d.getSsdCapacityGb() != null ? d.getSsdCapacityGb() : d.getStorageTotalGb();
        if (cap != null && cap.compareTo(BigDecimal.ZERO) > 0) {
            int val = cap.intValue();
            if (val >= 1000) {
                return (val / 1000) + " TB";
            }
            return val + " GB";
        }
        return "512 GB";
    }

    private BigDecimal parseRamGb(String ramStr) {
        if (ramStr == null || ramStr.isBlank()) return BigDecimal.valueOf(16);
        String cleaned = ramStr.replaceAll("[^0-9]", "");
        try {
            int val = Integer.parseInt(cleaned);
            return BigDecimal.valueOf(val > 0 ? val : 16);
        } catch (Exception e) {
            return BigDecimal.valueOf(16);
        }
    }

    private BigDecimal parseSsdGb(String ssdStr) {
        if (ssdStr == null || ssdStr.isBlank()) return BigDecimal.valueOf(512);
        String lower = ssdStr.toLowerCase();
        String cleaned = lower.replaceAll("[^0-9]", "");
        try {
            int val = Integer.parseInt(cleaned);
            if (lower.contains("tb")) {
                return BigDecimal.valueOf(val * 1024L);
            }
            return BigDecimal.valueOf(val > 0 ? val : 512);
        } catch (Exception e) {
            return BigDecimal.valueOf(512);
        }
    }
}