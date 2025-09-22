package com.example.master.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "cdpo_supplier_dispatch")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
public class CDPOSupplierDispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "demand_id", nullable = false)
    private Long demandId;

    @ManyToOne
    @JoinColumn(name = "dispatch_detail_id", nullable = false)
    @JsonBackReference
    private DispatchDetail dispatchDetail;

    @OneToMany(mappedBy = "awcDispatch", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<AWCDispatch> awcDispatches;

    @Column(name = "sector")
    private String sector;

    @Column(name = "dispatch_packets")
    private Integer dispatchPackets;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "sublot_no", unique = true, nullable = false)
    private String sublotNo;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public Integer getDispatchPackets() { return dispatchPackets; }
    public void setDispatchPackets(Integer dispatchPackets) { this.dispatchPackets = dispatchPackets; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getSublotNo() { return sublotNo; }
    public void setSublotNo(String sublotNo) { this.sublotNo = sublotNo; }
}
