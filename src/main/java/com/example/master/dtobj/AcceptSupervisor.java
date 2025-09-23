package com.example.master.dtobj;

import lombok.Data;

@Data
public class AcceptSupervisor {
    private Long id;
    private Long dispatchId;
    private Integer acceptedPackets;
    private String remarks;
}
