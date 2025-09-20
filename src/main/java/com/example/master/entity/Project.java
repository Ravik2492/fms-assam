package com.example.master.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "projects")
@Data
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_code", nullable = false, unique = true)
    private String projectCode;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Transient
    private String cdpoName;

    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    public String getCdpoName() {
        return projectName;
    }
}

