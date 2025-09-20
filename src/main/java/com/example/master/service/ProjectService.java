package com.example.master.service;

import com.example.master.entity.District;
import com.example.master.entity.Project;
import com.example.master.repository.DistrictRepository;
import com.example.master.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepo;
    private final DistrictRepository districtRepo;

    public ProjectService(ProjectRepository projectRepo, DistrictRepository districtRepo) {
        this.projectRepo = projectRepo;
        this.districtRepo = districtRepo;
    }

    public Project addProject(Project project) {
        if (projectRepo.existsByProjectCode(project.getProjectCode())) {
            throw new IllegalArgumentException("Project code already exists");
        }
        Long districtId = project.getDistrict().getId();
        District district = districtRepo.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("District not found"));
        project.setDistrict(district);
        return projectRepo.save(project);
    }

    public Project updateProject(Long id, Project updated) {
        Project existing = projectRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        existing.setProjectCode(updated.getProjectCode());
        existing.setProjectName(updated.getProjectName());

        Long districtId = updated.getDistrict().getId();
        District district = districtRepo.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("District not found"));
        existing.setDistrict(district);

        return projectRepo.save(existing);
    }

    public void deleteProject(Long id) {
        projectRepo.deleteById(id);
    }

    public List<Project> listProjects() {
        return projectRepo.findAll();
    }

    public List<Project> findByDistrictIdIn(List<Long> districtIds) {
        return projectRepo.findByDistrictIdIn(districtIds);
    }
}

