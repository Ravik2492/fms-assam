package com.example.master.controller;

import com.example.master.dtobj.DistrictBreakdownDto;
import com.example.master.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN','NODAL')")
@Tag(name = "FMS RJ-DASHBOARD", description = "APIs to get dashboard data")
public class DashboardController {

   /* @Autowired
    private DashboardService dashboardService;

    @GetMapping("/totals")
    @PreAuthorize("hasAnyRole('ADMIN','AWC','NODAL','SUPPLIER') or hasAuthority('ROLE_MANAGE_USERS')")
    @Operation(summary = "Get total counts of AWCs, Sectors, Projects, and Demands")
    public Map<String, Long> getTotalCounts() {
        return dashboardService.getTotalCounts();
    }

    @GetMapping("/district-breakdown")
    @PreAuthorize("hasAnyRole('ADMIN','AWC','NODAL','SUPPLIER') or hasAuthority('ROLE_MANAGE_USERS')")
    @Operation(summary = "Get number of sectors and projects in each district")
    public List<DistrictBreakdownDto> getDistrictBreakdown() {
        return dashboardService.getDistrictBreakdown();
    }
    */
}

