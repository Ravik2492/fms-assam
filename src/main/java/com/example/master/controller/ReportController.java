package com.example.master.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "FMS RJ-REPORTS", description = "APIs to get reports")
public class ReportController {

    /*@Autowired
    private DemandService demandService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','NODAL','AWC','SUPPLIER')")
    public ResponseEntity<List<ReportResponse>> getDemands() {
        return ResponseEntity.ok(demandService.getReportsData());
    }*/

}
