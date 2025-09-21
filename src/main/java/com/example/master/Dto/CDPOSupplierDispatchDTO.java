package com.example.master.Dto;

import com.example.master.model.DispatchDetail;
import lombok.Data;

@Data
public class CDPOSupplierDispatchDTO {

    private Long id;
    private Long demandId;
    private Long dispatchDetailId;
    private String sector;
    private Integer dispatchPackets;
    private String remarks;
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
