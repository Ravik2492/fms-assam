package com.example.master.dtobj;

import lombok.Data;

@Data
public class AwcCsvRow {
    private Long id;
    private String centerId;
    private String centerName;
    private Integer nutriBarCount;
    private Integer registeredChildren;
}
