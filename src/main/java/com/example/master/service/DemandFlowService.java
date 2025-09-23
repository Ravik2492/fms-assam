package com.example.master.service;

import com.example.master.Dto.AWCDispatchDTO;
import com.example.master.Dto.CDPOSupplierDispatchDTO;
import com.example.master.Dto.DispatchDetailDTO;
import com.example.master.config.TokenHelper;
import com.example.master.entity.AwcCenterr;
import com.example.master.entity.UserMetadata;
import com.example.master.model.AWCDispatch;
import com.example.master.model.CDPOSupplierDispatch;
import com.example.master.model.Demand;
import com.example.master.model.DispatchDetail;
import com.example.master.repository.*;
import com.example.master.services.DemandService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class DemandFlowService {

    @Autowired
    private DispatchDetailRepository dispatchDetailRepository;
    @Autowired
    private DemandService demandService;

    @Autowired
    private CDPOSupplierDispatchRepository repository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private UserMetadataRepository userMetadataRepository;

    @Autowired
    private AWCDispatchRepository awcDispatchRepository;

    @Autowired
    private AwcCenterRepository awcCenterRepository;

    @Autowired
    private DemandRepository demandRepository;

    public ResponseEntity<List<DispatchDetail>> createBulk(List<DispatchDetailDTO> dtos) {
        AtomicLong next = new AtomicLong(1);
        List<DispatchDetail> dispatches = dtos.stream().map(dto -> {
            DispatchDetail d = new DispatchDetail();
            d.setDemandId(dto.demandId);
            d.setBatchNumber(dto.batchNumber);
            d.setCdpoName(dto.cdpoName);
            d.setLotNumber("L-" + + next.getAndIncrement());
            d.setCdpoId(projectRepository.getById(dto.cdpoId));
            d.setNumberOfPackets(dto.numberOfPackets);
            d.setRemarks(dto.remarks);
            return d;
        }).toList();

        List<DispatchDetail> saved = dispatchDetailRepository.saveAll(dispatches);
        return ResponseEntity.status(201).body(saved);
    }

    public ResponseEntity<List<DispatchDetail>> listByDemand(Long demandId){

        String userid = TokenHelper.getUsername();
        UserMetadata metadata = userMetadataRepository.getById(userid);
        List<DispatchDetail> dispatchDetails = dispatchDetailRepository.findByDemandId(demandId).stream()
                .filter(dispatchDetail -> dispatchDetail.getCdpoId().getId().equals(Long.valueOf(metadata.getProjectId()))).toList();
        return ResponseEntity.ok(dispatchDetails);
    }

    public ResponseEntity<List<DispatchDetail>> listByDemandAWC(Long demandId){

        String userid = TokenHelper.getUsername();
        UserMetadata metadata = userMetadataRepository.getById(userid);
        Long projectId = Long.valueOf(metadata.getProjectId());
        Long sectorId = Long.valueOf(metadata.getSectorId());

        List<DispatchDetail> dispatchDetails = dispatchDetailRepository.findByDemandId(demandId).stream()
                .filter(dispatchDetail -> dispatchDetail.getCdpoId().getId().equals(projectId))
                .filter(dispatchDetail -> dispatchDetail.getCdpoSupplierDispatches().stream()
                        .anyMatch(dispatch -> dispatch.getSectorr().getId().equals(sectorId)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dispatchDetails);
    }

    public ResponseEntity<DispatchDetail> accept(Long id, Integer acceptedPackets, String remarks){


        DispatchDetail detail = dispatchDetailRepository.getById(id);
        detail.setAcceptedPackets(acceptedPackets);
        detail.setRemainingPackets(acceptedPackets);
        detail.setAcceptedRemarks(remarks);
        DispatchDetail saved = dispatchDetailRepository.save(detail);
        return ResponseEntity.ok(detail);
    }

    public ResponseEntity<DispatchDetail> acceptAWC(Long id, Long dispatchId, Integer acceptedPackets, String remarks){

        DispatchDetail detail = dispatchDetailRepository.getById(id);

        Optional<CDPOSupplierDispatch> optionalDispatch = detail.getCdpoSupplierDispatches()
                .stream()
                .filter(csd -> csd.getId().equals(dispatchId))
                .findFirst();

        if (optionalDispatch.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CDPOSupplierDispatch cdpoSupplierDispatch = optionalDispatch.get();
        cdpoSupplierDispatch.setAcceptedPackets(acceptedPackets);
        cdpoSupplierDispatch.setRemainingPackets(acceptedPackets);
        cdpoSupplierDispatch.setAcceptedRemarks(remarks);

        repository.save(cdpoSupplierDispatch); // Use correct repository

        //Now return updated Dispatch detail entityentity
        return ResponseEntity.ok(detail);
    }

    public ResponseEntity<List<CDPOSupplierDispatch>> dispatch(List<CDPOSupplierDispatchDTO> dtos) {

        List<CDPOSupplierDispatch> dispatches = new ArrayList<>();
        AtomicLong next = new AtomicLong(1);
        for (CDPOSupplierDispatchDTO dto : dtos) {
            CDPOSupplierDispatch entity = new CDPOSupplierDispatch();

            DispatchDetail detail = dispatchDetailRepository.getById(dto.getDispatchDetailId());
            entity.setDispatchDetail(detail);
            entity.setDemandId(dto.getDemandId());
            entity.setSector(dto.getSector());
            if(dto.getSectorId()!=null) {
                entity.setSectorr(sectorRepository.getById(dto.getSectorId()));
            }
            entity.setDispatchPackets(dto.getDispatchPackets());
            entity.setRemarks(dto.getRemarks());

            // ✅ Auto-generate sublot number
            entity.setSublotNo("SL-" + + next.getAndIncrement());
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

    public ResponseEntity<CDPOSupplierDispatch> dispatchAWC(List<AWCDispatchDTO> dtos) {

        if(dtos.isEmpty()) {
            //no details found
        }
        AWCDispatchDTO dtoObj = dtos.get(0);
        CDPOSupplierDispatch cdpoSupplierDispatch = repository.findById(dtoObj.getCdpoSupplierDispatchId())
                .orElseThrow(() -> new EntityNotFoundException("CDPO Sector dispatch not found: " + dtoObj.getDemandId()));
        AwcCenterr center = awcCenterRepository.findById(dtoObj.getAwcId())
                .orElseThrow(() -> new EntityNotFoundException("AWC not found: " + dtoObj.getAwcId()));
        Demand demand = demandRepository.findById(dtoObj.getDemandId())
                .orElseThrow(() -> new EntityNotFoundException("Demand not found: " + dtoObj.getDemandId()));

        List<AWCDispatch> dispatches = new ArrayList<>();
        for (AWCDispatchDTO dto : dtos) {
            AWCDispatch entity = new AWCDispatch();
            entity.setSublotNo(cdpoSupplierDispatch.getSublotNo());
            entity.setBenficiaryCount(dto.getBenficiaryCount());
            entity.setDistributedPackets(dto.getDistributedPackets());
            entity.setCenterName(center.getCenterName());

            if(cdpoSupplierDispatch.getRemainingPackets()>=dto.getDistributedPackets()) {
                Integer remainingPacktes = cdpoSupplierDispatch.getRemainingPackets() - dto.getDistributedPackets();
                cdpoSupplierDispatch.setRemainingPackets(remainingPacktes);
                repository.save(cdpoSupplierDispatch);
            } else {
                throw new IllegalArgumentException("Dispatch packets "+dto.getDistributedPackets()+" exceed remaining packets."+cdpoSupplierDispatch.getRemainingPackets());
            }
            entity.setDemand(demand);
            entity.setCdpoSupplierDispatch(cdpoSupplierDispatch);
            entity.setAwcCenterr(center);

            AWCDispatch savedEntity = awcDispatchRepository.save(entity);
            dispatches.add(savedEntity);
        }
        // ✅ Update demand status (assuming all DTOs share the same demandId)
        demandService.updateStatus(dtos.get(0).getDemandId(), "AWC_DISTRIBUTED");
        return ResponseEntity.status(HttpStatus.CREATED).body(cdpoSupplierDispatch);
    }
}
