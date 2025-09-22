// DispatchDetailController.java
package com.example.master.controller;

import com.example.master.Dto.CDPOSupplierDispatchDTO;
import com.example.master.Dto.DispatchDetailDTO;
import com.example.master.config.TokenHelper;
import com.example.master.entity.Project;
import com.example.master.entity.UserMetadata;
import com.example.master.model.CDPOSupplierDispatch;
import com.example.master.model.DispatchDetail;
import com.example.master.repository.CDPOSupplierDispatchRepository;
import com.example.master.repository.DispatchDetailRepository;
import com.example.master.repository.ProjectRepository;
import com.example.master.repository.UserMetadataRepository;
import com.example.master.services.DemandService;
import com.example.master.services.DispatchDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/demand-dispatches")
public class DispatchDetailController {

    private final DispatchDetailService service;

    @Autowired
    private DispatchDetailRepository dispatchDetailRepository;
    @Autowired
    private DemandService demandService;

    @Autowired
    private CDPOSupplierDispatchRepository repository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserMetadataRepository userMetadataRepository;

    public DispatchDetailController(DispatchDetailService service){
        this.service = service;
    }


    @GetMapping("/by-demand/{demandId}")
    public ResponseEntity<List<DispatchDetail>> listByDemand(@PathVariable Long demandId){

        String userid = TokenHelper.getUsername();
        UserMetadata metadata = userMetadataRepository.getById(userid);
        List<DispatchDetail> dispatchDetails = service.findByDemandId(demandId).stream()
                .filter(dispatchDetail -> dispatchDetail.getCdpoId().getId().equals(Long.valueOf(metadata.getProjectId()))).toList();
        return ResponseEntity.ok(dispatchDetails);
    }

    @PostMapping("/accept/{id}")
    public ResponseEntity<DispatchDetail> accept(@PathVariable("id") Long id,
                                                 @RequestParam("acceptedPackets") Integer acceptedPackets, @RequestParam("remarks") String remarks){


        DispatchDetail detail = dispatchDetailRepository.getById(id);
        detail.setAcceptedPackets(acceptedPackets);
        detail.setRemainingPackets(acceptedPackets);
        detail.setAcceptedRemarks(remarks);

        DispatchDetail saved = service.createDispatch(detail);
        //demandService.updateStatus(dto.demandId, "CDPO_DISPATCHED");
        // ✅ return 201 CREATED with entity in body (no URI)
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<DispatchDetail>> createBulk(@RequestBody List<DispatchDetailDTO> dtos) {
        List<DispatchDetail> dispatches = dtos.stream().map(dto -> {
            DispatchDetail d = new DispatchDetail();
            d.setDemandId(dto.demandId);
            d.setBatchNumber(dto.batchNumber);
            d.setCdpoName(dto.cdpoName);

            d.setCdpoId(projectRepository.getById(dto.cdpoId));
            d.setNumberOfPackets(dto.numberOfPackets);
            d.setRemarks(dto.remarks);
            return d;
        }).toList();

        //demandService.updateStatus(dtos.get(0).demandId, "CDPO_DISPATCHED");
        List<DispatchDetail> saved = service.createDispatches(dispatches);
        return ResponseEntity.status(201).body(saved);
    }


    @PostMapping("/dispatch-to-sector")
    public ResponseEntity<List<CDPOSupplierDispatch>> dispatch(@RequestBody List<CDPOSupplierDispatchDTO> dtos) {

        List<CDPOSupplierDispatch> dispatches = new ArrayList<>();

        for (CDPOSupplierDispatchDTO dto : dtos) {
            CDPOSupplierDispatch entity = new CDPOSupplierDispatch();

            DispatchDetail detail = dispatchDetailRepository.getById(dto.getDispatchDetailId());
            entity.setDispatchDetail(detail);
            entity.setDemandId(dto.getDemandId());
            entity.setSector(dto.getSector());
            entity.setDispatchPackets(dto.getDispatchPackets());
            entity.setRemarks(dto.getRemarks());

            // ✅ Auto-generate sublot number
            entity.setSublotNo(generateNextSublotNo());

            // ✅ Update remaining packets
            if(detail.getRemainingPackets()>=dto.getDispatchPackets()) {
                Integer remainingPacktes = detail.getRemainingPackets() - dto.getDispatchPackets();
                detail.setRemainingPackets(remainingPacktes);
            } else {
                throw new IllegalArgumentException("Dispatch packets "+dto.getDispatchPackets()+" exceed remaining packets."+detail.getRemainingPackets());
            }
            dispatchDetailRepository.save(detail);

            // ✅ Save dispatch entity
            CDPOSupplierDispatch savedEntity = repository.save(entity);

            // ✅ Update DTO with generated values
            dto.setId(savedEntity.getId());
            dto.setSublotNo(savedEntity.getSublotNo());

            dispatches.add(savedEntity);
        }

        // ✅ Update demand status (assuming all DTOs share the same demandId)
        demandService.updateStatus(dtos.get(0).getDemandId(), "CDPO_DISPATCHED");

        return ResponseEntity.status(HttpStatus.CREATED).body(dispatches);
    }

    private String generateNextSublotNo() {
        String lastSublotNo = repository.findLastSublotNo();
        if (lastSublotNo == null) {
            return "SUBLOT-1";
        }
        try {
            int lastNumber = Integer.parseInt(lastSublotNo.replace("SUBLOT-", ""));
            return "SUBLOT-" + (lastNumber + 1);
        } catch (NumberFormatException e) {
            return "SUBLOT-1"; // fallback if malformed
        }
    }

}

//package com.example.master.controller;
//
//import com.example.master.Dto.DispatchDetailDTO;
//import com.example.master.services.DispatchService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/dispatch")
//public class DispatchController {
//
//    private final DispatchService dispatchService;
//
//    public DispatchController(DispatchService dispatchService) {
//        this.dispatchService = dispatchService;
//    }
//
////    @PostMapping
////    public ResponseEntity<DispatchDetailDTO> createDispatch(@RequestBody DispatchDetailDTO dto) {
////        return ResponseEntity.ok(dispatchService.saveDispatch(dto));
////    }
////
////    @GetMapping
////    public ResponseEntity<List<DispatchDetailDTO>> getAllDispatches() {
////        return ResponseEntity.ok(dispatchService.getAllDispatches());
////    }
//
//    @PostMapping
//    public ResponseEntity<?> createDispatch(@RequestBody List<DispatchDetailDTO> request) {
//        return ResponseEntity.ok(dispatchService.saveDispatchDetails(request));
//    }
//
//    @GetMapping
//    public ResponseEntity<?> getAllDispatch() {
//        return ResponseEntity.ok(dispatchService.getAllDispatchDetails());
//    }
//
//}
