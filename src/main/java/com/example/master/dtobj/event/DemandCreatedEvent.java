package com.example.master.dtobj.event;

import lombok.Data;

@Data
public class DemandCreatedEvent {

    private String demandId;
    private String district;
    private String awc;
    private String supplierName;
    private String summary;
}
