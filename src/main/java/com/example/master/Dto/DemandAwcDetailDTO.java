package com.example.master.Dto;

import com.example.master.model.DemandAwcDetail;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;

public class DemandAwcDetailDTO {

    @NotNull(message = "AWC ID is required")
    private Long awcId;

    private Integer quantity;

    private String type;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    //    @Positive(message = "Quantity must be positive")
//    private int hcmNumber; // maps to quantity in demand
//
//    @NotBlank(message = "Quantity unit is required")
//    private String hcmUnit; // maps to quantityUnit
//
//    @Positive(message = "Quantity must be positive")
//    private int thrNumber; // maps to quantity in demand
//
//    @NotBlank(message = "Quantity unit is required")
//    private String thrUnit; // maps to quantityUnit

    private AnganwadiCenterDTO anganwadi;


//    public DemandAwcDetailDTO(DemandAwcDetail detail) {
//        this.awcId = detail.getAnganwadi() != null ? detail.getAnganwadi().getId() : null;
//        this.hcmNumber = detail.getHcmNumber();
//        this.hcmUnit = detail.getHcmUnit();
//        this.thrNumber = detail.getThrNumber();
//        this.thrUnit = detail.getThrUnit();
//    }


    public AnganwadiCenterDTO getAnganwadi() {
        return anganwadi;
    }

    public void setAnganwadi(AnganwadiCenterDTO anganwadi) {
        this.anganwadi = anganwadi;
    }

//    public int getThrNumber() {
//        return thrNumber;
//    }
//
//    public void setThrNumber(int thrNumber) {
//        this.thrNumber = thrNumber;
//    }
//
//    public String getThrUnit() {
//        return thrUnit;
//    }
//
//    public void setThrUnit(String thrUnit) {
//        this.thrUnit = thrUnit;
//    }

    // Getters and Setters
    public Long getAwcId() { return awcId; }
    public void setAwcId(Long awcId) { this.awcId = awcId; }

//    public int getHcmNumber() { return hcmNumber; }
//    public void setHcmNumber(int hcmNumber) { this.hcmNumber = hcmNumber; }
//
//    public String getHcmUnit() { return hcmUnit; }
//    public void setHcmUnit(String hcmUnit) { this.hcmUnit = hcmUnit; }
}
