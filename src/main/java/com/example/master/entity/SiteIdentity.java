package com.example.master.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "site_identity")
@Data
public class SiteIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_address")
    private String siteAddress;

    @Column(name = "site_header_name")
    private String siteHeaderName;

    @Column(name = "logo_path")
    private String logoPath;

    @Column(name = "login_logo_path")
    private String loginLogoPath;

    @Column(name = "status")
    private String status;

    // Getters and setters
}

