package com.example.master.dtobj;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LabReportRequest {
    private String labName;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private LocalDate testDate;
    private String remarks;
}

