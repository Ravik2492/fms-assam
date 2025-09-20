package com.example.master.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "districts")
@Data
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "district_name", nullable = false, unique = true)
    private String districtName;

    @Transient
    private List<Project> cdpos = new ArrayList<>();

}

