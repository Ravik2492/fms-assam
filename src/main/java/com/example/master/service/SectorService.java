package com.example.master.service;

import com.example.master.entity.District;
import com.example.master.entity.Project;
import com.example.master.entity.Sectorr;
import com.example.master.repository.DistrictRepository;
import com.example.master.repository.ProjectRepository;
import com.example.master.repository.SectorRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SectorService {

    private final SectorRepository sectorRepo;
    private final DistrictRepository districtRepo;

    private final ProjectRepository projectRepo;

    public SectorService(SectorRepository sectorRepo, DistrictRepository districtRepo, ProjectRepository projectRepo) {
        this.sectorRepo = sectorRepo;
        this.districtRepo = districtRepo;
        this.projectRepo = projectRepo;
    }

    public Sectorr addSector(Sectorr sector) {
        District district = districtRepo.findById(sector.getDistrict().getId())
                .orElseThrow(() -> new IllegalArgumentException("District not found"));
        Project project = projectRepo.findById(sector.getProject().getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        sector.setDistrict(district);
        sector.setProject(project);
        return sectorRepo.save(sector);
    }

    public Sectorr updateSector(Long id, Sectorr updated) {
        Sectorr existing = sectorRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sector not found"));

        District district = districtRepo.findById(updated.getDistrict().getId())
                .orElseThrow(() -> new IllegalArgumentException("District not found"));

        Project project = projectRepo.findById(updated.getProject().getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        existing.setDistrict(district);
        existing.setProject(project);
        existing.setSectorName(updated.getSectorName());

        return sectorRepo.save(existing);
    }

    public void deleteSector(Long id) {
        sectorRepo.deleteById(id);
    }

    public List<Sectorr> listSectors() {
        return sectorRepo.findAll();
    }

    public List<Sectorr> findByDistrictIdIn(List<Long> districtIds) {
        return sectorRepo.findByDistrictIdIn(districtIds);
    }

    public List<Sectorr> findByProjectIdIn(List<Long> projectIds) {
        return sectorRepo.findByProjectIdIn(projectIds);
    }
}
