package com.example.master.Dto;

import java.util.List;

public class SupplierMappingDTO {
    private String id;
    private String supplierId;
    private Long districtId;
    private List<Long> cdpoIds;
    private List<Long> sectorIds;

    // Getters & Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public List<Long> getCdpoIds() {
        return cdpoIds;
    }

    public void setCdpoIds(List<Long> cdpoIds) {
        this.cdpoIds = cdpoIds;
    }

    public List<Long> getSectorIds() {
        return sectorIds;
    }

    public void setSectorIds(List<Long> sectorIds) {
        this.sectorIds = sectorIds;
    }
}
