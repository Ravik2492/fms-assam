package com.example.master.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "lab_reports")
public class LabReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String labName;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private LocalDate testDate;
    private String status;
    private String remarks;

    private String filePath; // uploaded file path

    // ✅ Relation with BatchDetail (not IngredientDetail anymore)
//    @OneToOne(mappedBy = "labReport", fetch = FetchType.LAZY)
//    private BatchDetail batchDetail;

    @Column(name = "demand_id", nullable = false)
    private Long demandId;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLabName() { return labName; }
    public void setLabName(String labName) { this.labName = labName; }

    public LocalDate getManufacturingDate() { return manufacturingDate; }
    public void setManufacturingDate(LocalDate manufacturingDate) { this.manufacturingDate = manufacturingDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public LocalDate getTestDate() { return testDate; }
    public void setTestDate(LocalDate testDate) { this.testDate = testDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

//    public BatchDetail getBatchDetail() { return batchDetail; }
//    public void setBatchDetail(BatchDetail batchDetail) { this.batchDetail = batchDetail; }


    public Long getDemandId() {
        return demandId;
    }

    public void setDemandId(Long demandId) {
        this.demandId = demandId;
    }
}
