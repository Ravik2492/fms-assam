package com.example.master.dtobj;

import lombok.Data;

@Data
public class DistrictBreakdownDto {
    private Long districtId;
    private String districtName;
    private Long projectCount;
    private Long sectorCount;

    public DistrictBreakdownDto(Long districtId, String districtName, Long projectCount, Long sectorCount) {
        this.districtId = districtId;
        this.districtName = districtName;
        this.projectCount = projectCount;
        this.sectorCount = sectorCount;
    }

    // Getters and setters
}

