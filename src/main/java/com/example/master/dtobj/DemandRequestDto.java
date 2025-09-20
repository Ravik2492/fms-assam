package com.example.master.dtobj;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DemandRequestDto {
    private LocalDate fromDate;
    private LocalDate toDate;
    private String supplierId;
    private List<Long> districtIds;
    private String createdBy;
    private List<Long> projectIds;
    private List<Long> sectorIds;
    private List<DemandDetailDto> awcDetails;
}

