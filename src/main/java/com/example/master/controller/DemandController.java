package com.example.master.controller;

import com.example.master.Dto.*;
import com.example.master.entity.DemandStatuses;
import com.example.master.model.Demand;
import com.example.master.model.ProductCommodityQuantity;
import com.example.master.services.DemandService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/demands")
//@CrossOrigin(origins = "*")
public class DemandController {

//    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(DemandController.class);
    private final DemandService demandService;

    public DemandController(DemandService demandService,
                            KafkaTemplate<String, String> kafkaTemplate) {
        this.demandService = demandService;
        this.kafkaTemplate = kafkaTemplate;
    }

    // DWCD creates demand
    @PreAuthorize("hasAnyRole('ADMIN','DWCD')")
    @PostMapping
    public ResponseEntity<DemandResponseDTO> createDemand(@RequestPart DemandDTO dto,
                                                          @RequestPart("authorizationDoc") MultipartFile authorizationDoc,
                                                          @RequestPart("authorizationDocFci") MultipartFile authorizationDocFci) {
        logCurrentUserAuthorities("createDemand");
        Demand savedDemand = demandService.createDemand(dto, authorizationDoc, authorizationDocFci);

        DemandResponseDTO response = mapToResponseDTO(savedDemand);


        // 🔹 Build Kafka event payload
        String eventPayload = String.format(
                "{\"type\":\"NEW_DEMAND\",\"demandId\":\"%d\"}", savedDemand.getId()
        );

        // 🔹 Publish Kafka event immediately
        kafkaTemplate.send("demand-events", eventPayload).join(); // join() waits for ack


        return ResponseEntity.ok(response);
    }

    private DemandResponseDTO mapToResponseDTO(Demand demand) {
        DemandResponseDTO dto = new DemandResponseDTO();
        dto.setId(demand.getId());
        dto.setDescription(demand.getDescription());
        dto.setStatus(demand.getStatus());
        dto.setFromDate(demand.getFromDate());
        dto.setToDate(demand.getToDate());
        dto.setTotalDays(demand.getTotalDays()); // Assuming this field exists in the `Demand` entity
        dto.setNotes(demand.getNotes());
        dto.setFciDocs(demand.getFciDocs());
        dto.setSupplierDocs(demand.getSupplierDocs());
        dto.setCreatedAt(demand.getCreatedAt());
        dto.setUpdatedAt(demand.getUpdatedAt());

        // 🔹 demand category
        if (demand.getDemandCategory() != null) {
            DemandCategoryDTO cat = new DemandCategoryDTO();
            cat.setId(demand.getDemandCategory().getId());
            cat.setCategory(demand.getDemandCategory().getCategory());
            dto.setDemandCategory(cat);
        }

        // 🔹 beneficiary
        if (demand.getBeneficery() != null) {
            BeneficiaryDTO ben = new BeneficiaryDTO();
            ben.setId(demand.getBeneficery().getId());
            ben.setBeneficiaryName(demand.getBeneficery().getBeneficiaryName());
            dto.setBeneficiary(ben);
        }

        // 🔹 supplier
        if (demand.getSupplier() != null) {
            SupplierDTO sup = new SupplierDTO();
            sup.setId(demand.getSupplier());
            sup.setName(demand.getSupplierName());
            dto.setSupplier(sup);
        }

        // 🔹 district
//        if (demand.getDistrict() != null) {
//            DistrictDTO dist = new DistrictDTO();
//            dist.setId(demand.getDistrict().getId());
//            dist.setDistrictName(demand.getDistrict().getDistrictName());
//            dto.setDistrict(dist);
//        }

        // 🔹 fci
        if (demand.getFci() != null) {
            FciDTO fci = new FciDTO();
            fci.setId(demand.getFci().getId());
            fci.setName(demand.getFci().getName());
            dto.setFci(fci);
        }

        // 🔹 cdpo details
        if (demand.getCdpoDetails() != null) {
            List<DemandCdpoDetailResponseDTO> cdpoDtos = demand.getCdpoDetails().stream().map(cdpo -> {
                DemandCdpoDetailResponseDTO cdpoDto = new DemandCdpoDetailResponseDTO();
                cdpoDto.setId(cdpo.getId());
                cdpoDto.setCdpoId(cdpo.getCdpo().getId());
                cdpoDto.setCdpoName(cdpo.getCdpo().getProjectName());
                cdpoDto.setDistrictId(cdpo.getDistrict().getId());
                cdpoDto.setDistrictName(cdpo.getDistrict().getDistrictName());
                cdpoDto.setQuantity(cdpo.getQuantity());
                cdpoDto.setQuantityUnits(cdpo.getQuantityUnits());
                cdpoDto.setBeneficiaryCount(cdpo.getBeneficiaryCount());
                return cdpoDto;
            }).toList();
            dto.setCdpoDetails(cdpoDtos);
        }

        // 🔹 product + commodities
        if (demand.getDemandProducts() != null && !demand.getDemandProducts().isEmpty()) {
            String product = demand.getDemandProducts(); // only one
            ProductQuantityResponse pqDto = new ProductQuantityResponse();
//            pqDto.setDemandProductId(product.getId());
            pqDto.setProductType(product);

            Map<String, Double> commodities = demand.getProductQuantities().stream()
                    .collect(Collectors.toMap(
                            q -> q.getCommodity(),
                            ProductCommodityQuantity::getQuantity
                    ));
            pqDto.setCommodityQuantities(commodities);

            dto.setProductQuantity(pqDto);
        }

        return dto;
    }


    @PreAuthorize("hasAnyRole('ADMIN','DWCD','SUPPLIER')")
    @GetMapping
    public ResponseEntity<List<DemandResponseDTO>> getAllDemands() {
        return ResponseEntity.ok(demandService.getAllDemands());
    }

    @GetMapping("/fci-authorization-file/{id}")
    @Operation(summary = "Fetch authorization letter file by demand id")
    public ResponseEntity<Resource> serveeQRFile(@PathVariable Long id) {
        try {
            Resource resource = demandService.loadFCIReportById(id);

            String contentType = Files.probeContentType(resource.getFile().toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/supplier-file/{id}")
    @Operation(summary = "Fetch supplier report file by demand id")
    public ResponseEntity<Resource> serveQRFile(@PathVariable Long id) {
        try {
            Resource resource = demandService.loadSupplierReportById(id);

            String contentType = Files.probeContentType(resource.getFile().toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // All authenticated roles can view a specific demand
    @PreAuthorize("hasAnyRole('ADMIN','DWCD','FCI','SUPPLIER','CDPO','AWC','SUPERVISOR')")
    @GetMapping("/{id}")
    public ResponseEntity<Optional<DemandResponseDTO>> getDemandById(@PathVariable Long id) {
        logCurrentUserAuthorities("getDemandById");
        Optional<DemandResponseDTO> demand = demandService.getDemandById(id);
        return ResponseEntity.ok(demand);
    }


    @GetMapping("/track-demand/{id}")
    public ResponseEntity<List<DemandStatuses>> getDemandTrackById(@PathVariable Long id) {
        List<DemandStatuses> demand = demandService.getDemandStatusesById(id);
        return ResponseEntity.ok(demand);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DWCD','FCI','SUPPLIER','CDPO','AWC')")
    @PutMapping("/{demandId}/rejection-reason")
    public ResponseEntity<String> updateRejectionReason(
            @PathVariable Long demandId,
            @RequestBody RejectionReasonDTO rejectionReasonDTO) {

        demandService.updateRejectionReason(demandId, rejectionReasonDTO.getRejectionReason());
        return ResponseEntity.ok("Rejection reason updated successfully.");
    }


    // Updte quantity
//    @PreAuthorize("hasRole('FCI')")
//    @PutMapping("/{demandId}/quantity")
//    public ResponseEntity<Demand> updateQuantity(@PathVariable Long demandId, @RequestParam Integer newQuantity) {
//        Demand updatedDemand = demandService.updateQuantity(demandId, newQuantity);
//        return new ResponseEntity<>(updatedDemand, HttpStatus.OK);
//    }

    // *** CRITICAL: Admin only - delete demand with enhanced security ***
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDemand(@PathVariable Long id) {
        // Enhanced logging and security check
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("=== DELETE DEMAND REQUEST ===");
        logger.info("Demand ID: {}", id);
        logger.info("User: {}", auth != null ? auth.getName() : "UNKNOWN");

        if (auth != null) {
            List<String> authorities = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            logger.info("User authorities: {}", authorities);

            // Double-check admin role manually
            boolean hasAdminRole = authorities.contains("ROLE_ADMIN");
            logger.info("Has ADMIN role: {}", hasAdminRole);

            if (!hasAdminRole) {
                logger.error("SECURITY VIOLATION: Non-admin user {} attempted to delete demand {}",
                        auth.getName(), id);
                return ResponseEntity.status(403).body(Map.of(
                        "error", "Access Denied",
                        "message", "Only ADMIN users can delete demands"
                ));
            }
        } else {
            logger.error("SECURITY VIOLATION: Unauthenticated request to delete demand {}", id);
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "Authentication required"
            ));
        }

        try {
            demandService.deleteDemand(id);
            logger.info("Demand {} successfully deleted by admin user {}", id, auth.getName());
            return ResponseEntity.ok(Map.of(
                    "message", "Demand deleted successfully",
                    "deletedId", id.toString()
            ));
        } catch (Exception e) {
            logger.error("Error deleting demand {}: {}", id, e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Deletion Failed",
                    "message", e.getMessage()
            ));
        }
    }

    // FCI-specific endpoints
    @PreAuthorize("hasRole('FCI')")
    @GetMapping("/pending")
    public ResponseEntity<List<DemandResponseDTO>> getPendingDemands() {
        logCurrentUserAuthorities("getPendingDemands");
        List<DemandResponseDTO> demands = demandService.getPendingDemandsForFCI();
        return ResponseEntity.ok(demands);
    }

    @PreAuthorize("hasRole('FCI')")
    @PostMapping("/{id}/fci-decision")
    public ResponseEntity<Map<String, String>> fciDecision(@PathVariable Long id, @RequestBody DecisionRequest request) {
        logCurrentUserAuthorities("fciDecision");
        String status = request.isAccept() ? "FCI_ACCEPTED" : "FCI_REJECTED";
        demandService.updateStatus(id, status);

        String message = request.isAccept() ? "Demand accepted by FCI" : "Demand rejected by FCI";
        return ResponseEntity.ok(Map.of("message", message, "status", status));
    }

    @PreAuthorize("hasRole('FCI')")
    @PostMapping("/{id}/fci-dispatch")
    public ResponseEntity<Map<String, String>> fciDispatch(@PathVariable Long id) {
        logCurrentUserAuthorities("fciDispatch");
        demandService.updateStatus(id, "FCI_DISPATCHED");
        return ResponseEntity.ok(Map.of("message", "FCI dispatched to Supplier", "status", "FCI_DISPATCHED"));
    }

    // Supplier-specific endpoints
//    @PreAuthorize("hasRole('SUPPLIER')")
//    @GetMapping("/fci-accepted")
//    public ResponseEntity<List<DemandResponseDTO>> getAcceptedDemands() {
//        logCurrentUserAuthorities("getAcceptedDemands");
//        List<DemandResponseDTO> demands = demandService.getAcceptedDemandsForSupplier();
//        return ResponseEntity.ok(demands);
//    }

    @PreAuthorize("hasRole('SUPPLIER')")
    @GetMapping("/fci-accepted")
    public ResponseEntity<List<DemandResponseDTO>> getAcceptedDemands() {
        logCurrentUserAuthorities("getAcceptedDemands");
        return ResponseEntity.ok(demandService.getAcceptedDemandsForSupplier());
    }

    @PreAuthorize("hasRole('SUPPLIER')")
    @PostMapping("/{id}/supplier-decision")
    public ResponseEntity<Map<String, String>> supplierDecision(@PathVariable Long id, @RequestBody DecisionRequest request) {
        logCurrentUserAuthorities("supplierDecision");
        String status = request.isAccept() ? "SUPPLIER_ACCEPTED" : "SUPPLIER_REJECTED";
        demandService.updateStatus(id, status);

        String message = request.isAccept() ? "Supplier accepted and started manufacturing" : "Supplier rejected the demand";
        return ResponseEntity.ok(Map.of("message", message, "status", status));
    }

    @PreAuthorize("hasRole('SUPPLIER')")
    @PostMapping("/{id}/supplier-self-declare")
    public ResponseEntity<Map<String, String>> supplierSelfDeclare(@PathVariable Long id) {
        logCurrentUserAuthorities("supplierSelfDeclare");
        demandService.updateStatus(id, "SUPPLIER_SELF_DECLARED");

        return ResponseEntity.ok(Map.of(
                "message", "Supplier self-declared demand (bypassing FCI)",
                "status", "SUPPLIER_SELF_DECLARED"
        ));
    }

    @PreAuthorize("hasRole('SUPPLIER')")
    @PostMapping("/{id}/supplier-dispatch")
    public ResponseEntity<Map<String, String>> supplierDispatch(@PathVariable Long id) {
        logCurrentUserAuthorities("supplierDispatch");
        demandService.updateStatus(id, "SUPPLIER_DISPATCHED");
        return ResponseEntity.ok(Map.of("message", "Supplier dispatched to CDPO", "status", "SUPPLIER_DISPATCHED"));
    }

//     CDPO-specific endpoints
//    @PreAuthorize("hasRole('CDPO')")
//    @GetMapping("/manufactured")
//    public ResponseEntity<List<DemandResponseDTO>> getManufacturedDemands() {
//        logCurrentUserAuthorities("getManufacturedDemands");
//        List<DemandResponseDTO> demands = demandService.getManufacturedDemandsForCDPO();
//        return ResponseEntity.ok(demands);
//    }
    @PreAuthorize("hasRole('CDPO')")
    @GetMapping("/manufactured")
    public ResponseEntity<List<DemandResponseDTO>> getManufacturedDemands() {
        logCurrentUserAuthorities("getManufacturedDemands");
        return ResponseEntity.ok(demandService.getManufacturedDemandsForCDPO());
    }


    @PreAuthorize("hasRole('CDPO')")
    @PostMapping("/{id}/cdpo-decision")
    public ResponseEntity<Map<String, String>> cdpoDecision(@PathVariable Long id, @RequestBody DecisionRequest request) {
        logCurrentUserAuthorities("cdpoDecision");
        String status = request.isAccept() ? "CDPO_ACCEPTED" : "CDPO_REJECTED";
        demandService.updateStatus(id, status);

        String message = request.isAccept() ? "CDPO accepted the demand" : "CDPO rejected the demand";
        return ResponseEntity.ok(Map.of("message", message, "status", status));
    }


    @PreAuthorize("hasRole('CDPO')")
    @PostMapping("/{id}/cdpo-dispatch")
    public ResponseEntity<Map<String, String>> cdpoDispatch(@PathVariable Long id) {
        logCurrentUserAuthorities("cdpoDispatch");
        demandService.updateStatus(id, "CDPO_DISPATCHED");
        return ResponseEntity.ok(Map.of("message", "CDPO dispatched to AWC", "status", "CDPO_DISPATCHED"));
    }

    // AWC-specific endpoints
//    @PreAuthorize("hasRole('AWC')")
//    @GetMapping("/dispatched")
//    public ResponseEntity<List<DemandResponseDTO>> getDispatchedDemands() {
//        logCurrentUserAuthorities("getDispatchedDemands");
//        List<DemandResponseDTO> demands = demandService.getDispatchedDemandsForAWC();
//        return ResponseEntity.ok(demands);
//    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    @GetMapping("/dispatched-demands")
    public ResponseEntity<List<DemandResponseDTO>> getDispatchedDemandss() {
        logCurrentUserAuthorities("getDispatchedDemands");
        return ResponseEntity.ok(demandService.getDispatchedDemandsForAWCC());
    }

    @PreAuthorize("hasRole('AWC')")
    @GetMapping("/dispatched")
    public ResponseEntity<List<DemandResponseDTO>> getDispatchedDemands() {
        logCurrentUserAuthorities("getDispatchedDemands");
        return ResponseEntity.ok(demandService.getDispatchedDemandsForAWC());
    }

    @PreAuthorize("hasRole('AWC')")
    @PostMapping("/{id}/awc-accept")
    public ResponseEntity<Map<String, String>> awcAccept(@PathVariable Long id) {
        logCurrentUserAuthorities("awcAccept");
        demandService.updateStatus(id, "AWC_ACCEPTED");
        return ResponseEntity.ok(Map.of("message", "AWC accepted and distributed to beneficiaries", "status", "AWC_ACCEPTED"));
    }

    // Dashboard endpoints for different roles
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/dashboard")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        logCurrentUserAuthorities("getAdminDashboard");
        List<DemandResponseDTO> allDemands = demandService.getAllDemands();

        long pending = allDemands.stream().filter(d -> "PENDING".equals(d.getStatus())).count();
        long fciAccepted = allDemands.stream().filter(d -> "FCI_ACCEPTED".equals(d.getStatus())).count();
        long supplierAccepted = allDemands.stream().filter(d -> "SUPPLIER_ACCEPTED".equals(d.getStatus())).count();
        long dispatched = allDemands.stream().filter(d -> "CDPO_DISPATCHED".equals(d.getStatus())).count();
        long completed = allDemands.stream().filter(d -> "AWC_ACCEPTED".equals(d.getStatus())).count();
        long rejected = allDemands.stream().filter(d -> d.getStatus().endsWith("_REJECTED")).count();

        Map<String, Object> dashboard = Map.of(
                "total", allDemands.size(),
                "pending", pending,
                "fci_accepted", fciAccepted,
                "supplier_accepted", supplierAccepted,
                "dispatched", dispatched,
                "completed", completed,
                "rejected", rejected,
                "demands", allDemands
        );

        return ResponseEntity.ok(dashboard);
    }


    @PreAuthorize("hasAnyRole('ADMIN','DWCD','FCI','SUPPLIER','CDPO','AWC')")
    @GetMapping("/{id}/status-history")
    public ResponseEntity<Map<String, Object>> getStatusHistory(@PathVariable Long id) {
        logCurrentUserAuthorities("getStatusHistory");

        Optional<DemandResponseDTO> demandResponseDTOOpt = demandService.getDemandById(id);

        if (demandResponseDTOOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        DemandResponseDTO dto = demandResponseDTOOpt.get();

        // Build status timeline list
        List<Map<String, Object>> statusHistory = new java.util.ArrayList<>();

        if (dto.getCreatedAt() != null) {
            statusHistory.add(Map.of("status", "PENDING", "timestamp", dto.getCreatedAt()));
        }
        if (dto.getFciAcceptedAt() != null) {
            statusHistory.add(Map.of("status", "FCI_ACCEPTED", "timestamp", dto.getFciAcceptedAt()));
        }
        if (dto.getFciRejectedAt() != null) {
            statusHistory.add(Map.of("status", "FCI_REJECTED", "timestamp", dto.getFciRejectedAt()));
        }
        if (dto.getFciDispatchedAt() != null) {
            statusHistory.add(Map.of("status", "FCI_DISPATCHED", "timestamp", dto.getFciDispatchedAt()));
        }
        if (dto.getSupplierAcceptedAt() != null) {
            statusHistory.add(Map.of("status", "SUPPLIER_ACCEPTED", "timestamp", dto.getSupplierAcceptedAt()));
        }
        if (dto.getSupplierRejectedAt() != null) {
            statusHistory.add(Map.of("status", "SUPPLIER_REJECTED", "timestamp", dto.getSupplierRejectedAt()));
        }
        if (dto.getSupplierSelfDeclaredAt() != null) {
            statusHistory.add(Map.of("status", "SUPPLIER_SELF_DECLARED", "timestamp", dto.getSupplierSelfDeclaredAt()));
        }
        if (dto.getSupplierDispatchedAt() != null) {
            statusHistory.add(Map.of("status", "SUPPLIER_DISPATCHED", "timestamp", dto.getSupplierDispatchedAt()));
        }
//        if (dto.getCdpoAcceptedAt() != null) {
//            statusHistory.add(Map.of("status", "CDPO_ACCEPTED", "timestamp", dto.getCdpoAcceptedAt()));
//        }
//        if (dto.getCdpoRejectedAt() != null) {
//            statusHistory.add(Map.of("status", "CDPO_REJECTED", "timestamp", dto.getCdpoRejectedAt()));
//        }
        if (dto.getCdpoDispatchedAt() != null) {
            statusHistory.add(Map.of("status", "CDPO_DISPATCHED", "timestamp", dto.getCdpoDispatchedAt()));
        }
        if (dto.getAwcAcceptedAt() != null) {
            statusHistory.add(Map.of("status", "AWC_ACCEPTED", "timestamp", dto.getAwcAcceptedAt()));
        }

        // Response with timeline
        Map<String, Object> response = Map.of(
                "id", dto.getId(),
                "currentStatus", dto.getStatus(),
                "statusHistory", statusHistory
        );

        return ResponseEntity.ok(response);
    }


    // Status tracking endpoint
//    @PreAuthorize("hasAnyRole('ADMIN','DWCD','FCI','SUPPLIER','CDPO','AWC')")
//    @GetMapping("/{id}/status-history")
//    public ResponseEntity<Map<String, Object>> getStatusHistory(@PathVariable Long id) {
//        logCurrentUserAuthorities("getStatusHistory");
//
//        // Fetch the demand response DTO
//        Optional<DemandResponseDTO> demandResponseDTOOpt = demandService.getDemandById(id);
//
//        // If no demand is found, return 404
//        if (demandResponseDTOOpt.isEmpty()) {
////            log.debug("Demand with ID " + id + " not found.");
//            return ResponseEntity.notFound().build();
//        }
//
//        // Map the DTO to a Demand entity
//        DemandResponseDTO demandResponseDTO = demandResponseDTOOpt.get();
////        log.debug("Fetched demand response: " + demandResponseDTO);
//
//        Demand demand = new Demand();
//        demand.setId(demandResponseDTO.getId());
//        demand.setStatus(demandResponseDTO.getStatus());
//        demand.setCreatedAt(demandResponseDTO.getCreatedAt());
//        demand.setFciAcceptedAt(demandResponseDTO.getFciAcceptedAt());
//        demand.setFciRejectedAt(demandResponseDTO.getFciRejectedAt());
//        demand.setSupplierAcceptedAt(demandResponseDTO.getSupplierAcceptedAt());
//        demand.setSupplierRejectedAt(demandResponseDTO.getSupplierRejectedAt());
//        demand.setCdpoDispatchedAt(demandResponseDTO.getCdpoDispatchedAt());
//        demand.setAwcAcceptedAt(demandResponseDTO.getAwcAcceptedAt());
//
////        log.debug("Mapped Demand object: " + demand);
//
//        // Build the status history map
//        Map<String, Object> history = Map.of(
//                "id", demand.getId() != null ? demand.getId() : "N/A",
//                "currentStatus", demand.getStatus() != null ? demand.getStatus() : "Unknown",
//                "createdAt", demand.getCreatedAt() != null ? demand.getCreatedAt() : "Unknown",
//                "fciAcceptedAt", demand.getFciAcceptedAt() != null ? demand.getFciAcceptedAt() : "Unknown",
//                "fciRejectedAt", demand.getFciRejectedAt() != null ? demand.getFciRejectedAt() : "Unknown",
//                "supplierAcceptedAt", demand.getSupplierAcceptedAt() != null ? demand.getSupplierAcceptedAt() : "Unknown",
//                "supplierRejectedAt", demand.getSupplierRejectedAt() != null ? demand.getSupplierRejectedAt() : "Unknown",
//                "cdpoDispatchedAt", demand.getCdpoDispatchedAt() != null ? demand.getCdpoDispatchedAt() : "Unknown",
//                "awcAcceptedAt", demand.getAwcAcceptedAt() != null ? demand.getAwcAcceptedAt() : "Unknown"
//        );
//
//        return ResponseEntity.ok(history);
//    }

    /**
     * Helper method to log current user's authorities for debugging
     */
    private void logCurrentUserAuthorities(String operation) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            List<String> authorities = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            logger.info("Operation: {} | User: {} | Authorities: {}",
                    operation, auth.getName(), authorities);
        } else {
            logger.warn("Operation: {} | No authentication found", operation);
        }
    }
}