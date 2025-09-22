package com.example.master.controller;

import com.example.master.entity.District;
import com.example.master.service.DistrictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/districts")
@Tag(name = "FMS RJ-DISTRICT", description = "APIs to control districts operations")
public class DistrictController {

    private final DistrictService service;

    public DistrictController(DistrictService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create New district")
    public District addDistrict(@RequestBody District district) {
        return service.addDistrict(district);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update district")
    public District updateDistrict(@PathVariable Long id, @RequestBody District district) {
        return service.updateDistrict(id, district);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete district")
    public void deleteDistrict(@PathVariable Long id) {
        service.deleteDistrict(id);
    }

    //@PreAuthorize("hasAnyRole('ADMIN','AWC','NODAL','SUPPLIER','SUPERVISOR')")
    @GetMapping
    @Operation(summary = "List districts")
    public List<District> listDistricts() {
        return service.listDistricts();
    }
}
