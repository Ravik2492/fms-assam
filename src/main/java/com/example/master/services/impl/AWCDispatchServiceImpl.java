package com.example.master.services.impl;

import com.example.master.Dto.AWCDispatchDTO;
import com.example.master.entity.AwcCenterr;
import com.example.master.model.AWCDispatch;
import com.example.master.model.AnganwadiCenter;
import com.example.master.model.CDPOSupplierDispatch;
import com.example.master.model.Demand;
import com.example.master.repository.*;
import com.example.master.services.AWCDispatchService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AWCDispatchServiceImpl implements AWCDispatchService {

    private final AWCDispatchRepository dispatchRepository;
    private final AnganwadiCenterRepository centerRepository;
    private final DemandRepository demandRepository;

    @Autowired
    private AwcCenterRepository awcCenterRepository;

    @Autowired
    private CDPOSupplierDispatchRepository cdpoSupplierDispatchRepository;

    public AWCDispatchServiceImpl(AWCDispatchRepository dispatchRepository,
                                  AnganwadiCenterRepository centerRepository,
                                  DemandRepository demandRepository) {
        this.dispatchRepository = dispatchRepository;
        this.centerRepository = centerRepository;
        this.demandRepository = demandRepository;
    }

    @Override
    public AWCDispatchDTO createDispatch(AWCDispatchDTO dto) {
        AWCDispatch entity = mapDtoToEntity(dto);
        return mapEntityToDto(dispatchRepository.save(entity));
    }

    @Override
    public List<AWCDispatchDTO> createDispatches(List<AWCDispatchDTO> dtos) {




        List<AWCDispatch> entities = dtos.stream()
                .map(this::mapDtoToEntity)
                .collect(Collectors.toList());

        List<AWCDispatch> savedEntities = dispatchRepository.saveAll(entities);
        return savedEntities.stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public AWCDispatchDTO getDispatchById(Long id) {
        return dispatchRepository.findById(id)
                .map(this::mapEntityToDto)
                .orElseThrow(() -> new EntityNotFoundException("Dispatch not found with id " + id));
    }

    @Override
    public List<AWCDispatchDTO> getAllDispatches() {
        return dispatchRepository.findAll().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public AWCDispatchDTO updateDispatch(Long id, AWCDispatchDTO dto) {
        AWCDispatch existing = dispatchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dispatch not found with id " + id));

        existing.setSublotNo(dto.getSublotNo());
        existing.setBenficiaryCount(dto.getBenficiaryCount());
        existing.setDistributedPackets(dto.getDistributedPackets());

        // Update demand if provided
        if (dto.getDemandId() != null) {
            Demand demand = demandRepository.findById(dto.getDemandId())
                    .orElseThrow(() -> new EntityNotFoundException("Demand not found: " + dto.getDemandId()));
            existing.setDemand(demand);
        }

        // Update Anganwadi center if provided
        if (dto.getAnganwadiId() != null) {
            AwcCenterr center = awcCenterRepository.findById(dto.getAnganwadiId())
                    .orElseThrow(() -> new EntityNotFoundException("AWC not found: " + dto.getAnganwadiId()));
            existing.setAwcCenterr(center);
        }

        return mapEntityToDto(dispatchRepository.save(existing));
    }

    @Override
    public void deleteDispatch(Long id) {
        if (!dispatchRepository.existsById(id)) {
            throw new EntityNotFoundException("Dispatch not found with id " + id);
        }
        dispatchRepository.deleteById(id);
    }

    // ====================== Mapper Methods ======================
    private AWCDispatchDTO mapEntityToDto(AWCDispatch entity) {
        AWCDispatchDTO dto = new AWCDispatchDTO();
        dto.setId(entity.getId());
        dto.setSublotNo(entity.getSublotNo());
        dto.setBenficiaryCount(entity.getBenficiaryCount());
        dto.setDistributedPackets(entity.getDistributedPackets());

        if (entity.getDemand() != null) {
            dto.setDemandId(entity.getDemand().getId());
//            dto.setDemandName(entity.getDemand().getName()); // optional
        }

        if (entity.getAwcCenterr() != null) {
            dto.setAnganwadiId(entity.getAwcCenterr().getId());
            dto.setAnganwadiName(entity.getAwcCenterr().getCenterName());
        }

        return dto;
    }

    private AWCDispatch mapDtoToEntity(AWCDispatchDTO dto) {
        AWCDispatch entity = new AWCDispatch();
        entity.setSublotNo(dto.getSublotNo());
        entity.setBenficiaryCount(dto.getBenficiaryCount());
        entity.setDistributedPackets(dto.getDistributedPackets());

        if (dto.getDemandId() != null) {
            Demand demand = demandRepository.findById(dto.getDemandId())
                    .orElseThrow(() -> new EntityNotFoundException("Demand not found: " + dto.getDemandId()));
            entity.setDemand(demand);
        }

        if (dto.getCdpoSupplierDispatchId() != null) {
            CDPOSupplierDispatch cdpoSupplierDispatch = cdpoSupplierDispatchRepository.findById(dto.getCdpoSupplierDispatchId())
                    .orElseThrow(() -> new EntityNotFoundException("CDPO Sector dispatch not found: " + dto.getDemandId()));
            entity.setCdpoSupplierDispatch(cdpoSupplierDispatch);
        }



        if (dto.getAnganwadiId() != null) {
            AwcCenterr center = awcCenterRepository.findById(dto.getAnganwadiId())
                    .orElseThrow(() -> new EntityNotFoundException("AWC not found: " + dto.getAnganwadiId()));
            entity.setAwcCenterr(center);
        }

        return entity;
    }
}
