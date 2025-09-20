package com.example.master.dtobj;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DemandResponse {
    private Long id;
    private String demandId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String supplierName;
    private LocalDateTime updatedAt;
    private DemandStatus demandStatus;
}
