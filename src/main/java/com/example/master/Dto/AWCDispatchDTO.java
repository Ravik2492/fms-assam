package com.example.master.Dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AWCDispatchDTO {
    private Long id;
    private String sublotNo;
    private Integer benficiaryCount;
    private Integer distributedPackets;
    private LocalDateTime createdAt;

    private Long cdpoSupplierDispatchId;

    private Long demandId;
    //private String demandName; // From Demand entity

    private Long anganwadiId;
    private String anganwadiName;
//    private List<Long> anganwadiCenters;
//    private List<AnganwadiCenterDTO> anganwadiCentersData;

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


}
