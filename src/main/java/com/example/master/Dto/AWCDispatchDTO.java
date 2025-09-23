package com.example.master.Dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AWCDispatchDTO {
    private Integer benficiaryCount;
    private Integer distributedPackets;
    private Long cdpoSupplierDispatchId;
    private Long demandId;
    private Long awcId;



}
