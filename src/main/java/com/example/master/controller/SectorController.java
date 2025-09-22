package com.example.master.controller;

import com.example.master.entity.Sectorr;
import com.example.master.service.SectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sectors")
@Tag(name = "FMS RJ-SEC", description = "APIs to control sector operations")
public class SectorController {

    private final SectorService service;

    public SectorController(SectorService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create New sector")
    public Sectorr addSector(@RequestBody Sectorr sector) {
        return service.addSector(sector);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update sector")
    public Sectorr updateSector(@PathVariable Long id, @RequestBody Sectorr sector) {
        return service.updateSector(id, sector);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete sector")
    public void deleteSector(@PathVariable Long id) {
        service.deleteSector(id);
    }

    //@PreAuthorize("hasAnyRole('ADMIN','AWC','NODAL','SUPPLIER','SUPERVISOR')")
    @GetMapping
    @Operation(summary = "List sectors")
    public List<Sectorr> listSectors() {
        return service.listSectors();
    }

    //@PreAuthorize("hasAnyRole('ADMIN','AWC','NODAL','SUPPLIER','SUPERVISOR')")
    @GetMapping("/byDistrictIds")
    @Operation(summary = "List sectors By District Ids")
    public List<Sectorr> listSectorsByDistrictIds(@RequestParam List<Long> districtIds) {
        return service.findByDistrictIdIn(districtIds);
    }

    //@PreAuthorize("hasAnyRole('ADMIN','AWC','NODAL','SUPPLIER','SUPERVISOR')")
    //@PreAuthorize("hasAnyRole('ADMIN','AWC','NODAL','SUPPLIER','SUPERVISOR')")
    @GetMapping("/byProjectIds")
    @Operation(summary = "List sectors By Project Ids")
    public List<Sectorr> listSectorsByProjectIds(@RequestParam List<Long> projectIds) {
        return service.findByProjectIdIn(projectIds);
    }
}

