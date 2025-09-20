package com.example.master.dtobj;

import lombok.Data;

@Data
public class UserMetadataRequest {
    private String districtId;
    private String sectorId;
    private String projectId;
    private String awc;
}

