package com.example.master.Dto;

public class DemandDispatchFCIDTO {

    private Long id;
    private Integer dispatchQuantity;
    private String remarks;
    private String supplierId;
    private Long warehouseId;
    private Long demandId;

    // Constructors, Getters, and Setters
    public DemandDispatchFCIDTO(Long id, Integer dispatchQuantity, String remarks, String supplierId, Long warehouseId, Long demandId) {
        this.id = id;
        this.dispatchQuantity = dispatchQuantity;
        this.remarks = remarks;
        this.supplierId = supplierId;
        this.warehouseId = warehouseId;
        this.demandId = demandId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDispatchQuantity() {
        return dispatchQuantity;
    }

    public void setDispatchQuantity(Integer dispatchQuantity) {
        this.dispatchQuantity = dispatchQuantity;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Long getDemandId() {
        return demandId;
    }

    public void setDemandId(Long demandId) {
        this.demandId = demandId;
    }
}
