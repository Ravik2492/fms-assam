package com.example.master.service;

import com.example.master.entity.District;
import com.example.master.entity.Project;
import com.example.master.repository.DistrictRepository;
import com.example.master.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class DistrictService {

    private final DistrictRepository repository;

    @Autowired
    private ProjectRepository projectRepository;

    public DistrictService(DistrictRepository repository) {
        this.repository = repository;
    }

    public District addDistrict(District district) {
        if (repository.existsByDistrictName(district.getDistrictName())) {
            throw new IllegalArgumentException("District already exists");
        }
        return repository.save(district);
    }

    public District updateDistrict(Long id, District updated) {
        District existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("District not found"));
        existing.setDistrictName(updated.getDistrictName());
        return repository.save(existing);
    }

    public void deleteDistrict(Long id) {
        repository.deleteById(id);
    }

    public List<District> listDistricts() {

        List<District> districts = repository.findAll();

        for (District district : districts) {
            List<Project> projects = projectRepository.findByDistrictIdIn(
                    Collections.singletonList(district.getId())
            );
            district.setCdpos(projects);
        }

        return districts;
    }
}
