package com.example.master.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "awc_centers")
@Data
public class AwcCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @Column(name = "center_id", nullable = false, unique = true)
    private String centerId;

    @Column(name = "center_name", nullable = false)
    private String centerName;

    @Column(name = "center_address")
    private String centerAddress;

    @Column(name = "center_email")
    private String centerEmail;

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "status")
    private String status;

    // Getters and setters
    // (Omitted here for brevity, but include all fields)
}
