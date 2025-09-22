package com.example.master.controller;

import com.example.master.entity.AwcCenterr;
import com.example.master.service.AwcCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/awc-centers")
@Tag(name = "FMS RJ-AWC", description = "APIs to control AWC operations")
public class AwcCenterController {

    private final AwcCenterService service;

    public AwcCenterController(AwcCenterService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create New AWC")
    public AwcCenterr addCenter(@RequestBody AwcCenterr center) {
        return service.addCenter(center);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update AWC")
    public AwcCenterr updateCenter(@PathVariable Long id, @RequestBody AwcCenterr center) {
        return service.updateCenter(id, center);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete AWC")
    public void deleteCenter(@PathVariable Long id) {
        service.deleteCenter(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','AWC','NODAL','SUPPLIER','SUPERVISOR')")
    @GetMapping
    @Operation(summary = "List AWCs")
    public List<AwcCenterr> listCenters() {
        return service.listCenters();
    }

    @PreAuthorize("hasAnyRole('ADMIN','AWC','NODAL','SUPPLIER','SUPERVISOR')")
    @GetMapping("/byDistrictSectorAndProjectIds")
    @Operation(summary = "List AWC centers by District, Sector, and Project IDs")
    public List<AwcCenterr> listAWCCenters(
            @RequestParam(required = false) List<Long> districtIds,
            @RequestParam(required = false) List<Long> sectorIds,
            @RequestParam(required = false) List<Long> projectIds) {
        return service.findByFilters(districtIds, sectorIds, projectIds);
    }

}
