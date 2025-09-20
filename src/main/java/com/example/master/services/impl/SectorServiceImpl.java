package com.example.master.services.impl;

import com.example.master.Dto.AnganwadiCenterDTO;
import com.example.master.Dto.SectorDTO;
import com.example.master.model.Cdpos;
import com.example.master.model.Sector;
import com.example.master.repository.CdpoRepository;
import com.example.master.repository.SectorRepository;
import com.example.master.services.SectorService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SectorServiceImpl implements SectorService {

    private final SectorRepository sectorRepository;
    private final CdpoRepository cdpoRepository;

    public SectorServiceImpl(SectorRepository sectorRepository, CdpoRepository cdpoRepository) {
        this.sectorRepository = sectorRepository;
        this.cdpoRepository = cdpoRepository;
    }

    @Override
    public SectorDTO createSector(SectorDTO sectorDTO) {
        Cdpos cdpo = cdpoRepository.findById(sectorDTO.getCdpoId())
                .orElseThrow(() -> new RuntimeException("Cdpo not found"));

        Sector sector = new Sector();
        sector.setCdpo(cdpo);
        sector.setName(sectorDTO.getName());
        sector.setStatus(sectorDTO.getStatus());

        Sector saved = null;//sectorRepository.save(sector);
        return mapToDTO(saved);
    }

    @Override
    public SectorDTO updateSector(Long id, SectorDTO sectorDTO) {
        /*Sector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sector not found"));

        Cdpo cdpo = cdpoRepository.findById(sectorDTO.getCdpoId())
                .orElseThrow(() -> new RuntimeException("Cdpo not found"));

        sector.setCdpo(cdpo);
        sector.setName(sectorDTO.getName());
        sector.setStatus(sectorDTO.getStatus());

        Sector updated = sectorRepository.save(sector);
        return mapToDTO(updated);*/
        return null;//
    }

    @Override
    public void deleteSector(Long id) {
        sectorRepository.deleteById(id);
    }

    @Override
    public SectorDTO getSectorById(Long id) {
       /* return sectorRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Sector not found"));*/
        return null;
    }

    @Override
    public List<SectorDTO> getAllSectors() {
        return null;//
    }

    // ✅ New method: Get all sectors for a CDPO
    @Override
    public List<SectorDTO> getAllSectorsByCdpo(Long cdpoId) {
        return null;//sectorRepository.findByCdpoId(cdpoId)
                //.stream()
                //.map(this::mapToDTO)
                //.collect(Collectors.toList());
    }

    // ✅ New method: Get sector along with its anganwadies
    @Override
    public SectorDTO getSectorWithAnganwadies(Long sectorId) {
        /*Sector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new RuntimeException("Sector not found"));*/
        return null;//mapToDTO(sector);
    }

    // ✅ Helper method for mapping
    private SectorDTO mapToDTO(Sector sector) {
        List<AnganwadiCenterDTO> angList = sector.getAnganwadiCenters() != null
                ? sector.getAnganwadiCenters().stream()
                .map(a -> new AnganwadiCenterDTO(a.getId(), a.getCenterName(), a.getCenterAddress(), a.getStatus()))
                .collect(Collectors.toList())
                : List.of();

        return new SectorDTO(
                sector.getId(),
                sector.getCdpo().getId(),
                sector.getName(),
                sector.getStatus(),
                angList
        );
    }
}
