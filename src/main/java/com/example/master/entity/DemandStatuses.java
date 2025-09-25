package com.example.master.entity;

import com.example.master.model.Demand;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "demand_statuses")
public class DemandStatuses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@Enumerated(EnumType.STRING)
    @Column(name = "demand_status", nullable = false)
    private String demandStatus;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "status_date", nullable = false)
    private LocalDateTime statusDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demand_id", nullable = false)
    @JsonBackReference
    private Demand demandIds;

    // Constructors, getters, setters
}
