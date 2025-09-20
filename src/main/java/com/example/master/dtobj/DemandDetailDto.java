package com.example.master.dtobj;

import lombok.Data;

@Data
public class DemandDetailDto {
    private Long awcId;
    private Integer nutritionBarCount;
    private String nutritionBarPackageType;
    private Integer registeredChildren;
}
