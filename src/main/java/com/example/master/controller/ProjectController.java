package com.example.master.controller;

import com.example.master.entity.Project;
import com.example.master.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "FMS RJ-PROJ", description = "APIs to control project/cdpo operations")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create New project")
    public Project addProject(@RequestBody Project project) {
        return service.addProject(project);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update project")
    public Project updateProject(@PathVariable Long id, @RequestBody Project project) {
        return service.updateProject(id, project);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project")
    public void deleteProject(@PathVariable Long id) {
        service.deleteProject(id);
    }

    //@PreAuthorize("hasAnyRole('ADMIN','AWC','NODAL','SUPPLIER')")
    @GetMapping
    @Operation(summary = "List projects")
    public List<Project> listProjects() {
        return service.listProjects();
    }

    //@PreAuthorize("hasAnyRole('ADMIN','AWC','NODAL','SUPPLIER')")
    @GetMapping("/byDistrictIds")
    @Operation(summary = "List projects By District Ids")
    public List<Project> listProjectsByDistrictIds(@RequestParam List<Long> districtIds) {
        return service.findByDistrictIdIn(districtIds);
    }
}

