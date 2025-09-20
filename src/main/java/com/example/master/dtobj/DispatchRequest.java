package com.example.master.dtobj;

import lombok.Data;

@Data
public class DispatchRequest {
    private Long batchId;
    private String awcCenterId;
    private Integer requestedPackets;
    private Integer packetsToDispatch;
    private String remarks;
}
