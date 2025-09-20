package com.example.master.entity;

import com.example.master.dtobj.CDPOs;
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
    private List<CDPOs> cdpos = new ArrayList<>();

}

