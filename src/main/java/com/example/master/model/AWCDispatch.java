package com.example.master.model;

import com.example.master.entity.AwcCenterr;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "awc_dispatch")
@Data
public class AWCDispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "awc_centerss_id", nullable = false) // foreign key to AnganwadiCenter
    private AwcCenterr awcCenterr;

    private String centerName;

    @ManyToOne
    @JoinColumn(name = "cdpo_supplier_dispatch_id", nullable = false)
    @JsonBackReference
    private CDPOSupplierDispatch cdpoSupplierDispatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demand_id", nullable = false)
    private Demand demand;
    private Integer distributedPackets;
    private Integer benficiaryCount;
    private LocalDateTime createdAt;

    @Transient
    private String sublotNo;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
