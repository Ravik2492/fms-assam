package com.example.master.service;

import com.example.master.dtobj.KcUser;
import com.example.master.dtobj.LookupType;
import com.example.master.entity.AwcCenter;
import com.example.master.repository.AwcCenterRepository;
import com.example.master.repository.DistrictRepository;
import com.example.master.repository.ProjectRepository;
import com.example.master.repository.SectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AwcCenterService {

    private final AwcCenterRepository awcRepo;
    private final DistrictRepository districtRepo;
    private final ProjectRepository projectRepo;
    private final SectorRepository sectorRepo;

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    public AwcCenterService(
            AwcCenterRepository awcRepo,
            DistrictRepository districtRepo,
            ProjectRepository projectRepo,
            SectorRepository sectorRepo
    ) {
        this.awcRepo = awcRepo;
        this.districtRepo = districtRepo;
        this.projectRepo = projectRepo;
        this.sectorRepo = sectorRepo;
    }

    public AwcCenter addCenter(AwcCenter center) {
        if (awcRepo.existsByCenterId(center.getCenterId())) {
            throw new IllegalArgumentException("Center ID already exists");
        }
        KcUser user = keycloakAdminService.getUserDetails(center.getCenterId(), LookupType.ID);
        if(user==null) {
            throw new IllegalArgumentException("User not found for center: "+center.getCenterId());
        }
        center.setCenterEmail(user.getEmail());

        center.setDistrict(districtRepo.findById(center.getDistrict().getId())
                .orElseThrow(() -> new IllegalArgumentException("District not found")));
        center.setProject(projectRepo.findById(center.getProject().getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found")));
        center.setSector(sectorRepo.findById(center.getSector().getId())
                .orElseThrow(() -> new IllegalArgumentException("Sector not found")));

        return awcRepo.save(center);
    }

    public AwcCenter updateCenter(Long id, AwcCenter updated) {
        AwcCenter existing = awcRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AWC Center not found"));

        existing.setCenterId(updated.getCenterId());
        existing.setCenterName(updated.getCenterName());
        existing.setCenterAddress(updated.getCenterAddress());
        existing.setPincode(updated.getPincode());
        existing.setLatitude(updated.getLatitude());
        existing.setLongitude(updated.getLongitude());
        existing.setStatus(updated.getStatus());

        existing.setDistrict(districtRepo.findById(updated.getDistrict().getId())
                .orElseThrow(() -> new IllegalArgumentException("District not found")));
        existing.setProject(projectRepo.findById(updated.getProject().getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found")));
        existing.setSector(sectorRepo.findById(updated.getSector().getId())
                .orElseThrow(() -> new IllegalArgumentException("Sector not found")));

        return awcRepo.save(existing);
    }

    public void deleteCenter(Long id) {
        awcRepo.deleteById(id);
    }

    public List<AwcCenter> listCenters() {
        return awcRepo.findAll();
    }

    public List<AwcCenter> findByFilters(List<Long> districtIds, List<Long> sectorIds, List<Long> projectIds) {
        return awcRepo.findByFilters(districtIds, sectorIds, projectIds);
    }

}

