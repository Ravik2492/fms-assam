package com.example.master.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "awc_centers")
@Data
public class AwcCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="center_name", nullable = false)
    private String centerName;

    @ManyToOne
    @JoinColumn(name = "supervisor_id", nullable = false)
    private Supervisor supervisor;

}
