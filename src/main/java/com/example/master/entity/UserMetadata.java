package com.example.master.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_metadata")
public class UserMetadata {

    @Id
    private String userId; // Same as Keycloak user ID

    private String districtId;
    private String sectorId;
    private String projectId;
    private String awc;

    public UserMetadata() {}

    public UserMetadata(String userId, String districtId, String sectorId, String projectId, String awc) {
        this.userId = userId;
        this.districtId = districtId;
        this.sectorId = sectorId;
        this.projectId = projectId;
        this.awc = awc;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDistrictId() { return districtId; }
    public void setDistrictId(String districtId) { this.districtId = districtId; }

    public String getSectorId() { return sectorId; }
    public void setSectorId(String sectorId) { this.sectorId = sectorId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getAwc() { return awc; }
    public void setAwc(String awc) { this.awc = awc; }
}

