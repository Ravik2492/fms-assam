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
    private Long cdpoSupplierDispatchId;
    private Long demandId;
    private Long anganwadiId;
    private String anganwadiName;



}
