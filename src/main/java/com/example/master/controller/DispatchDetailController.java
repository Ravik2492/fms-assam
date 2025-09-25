package com.example.master.controller;

import com.example.master.Dto.AWCDispatchDTO;
import com.example.master.Dto.CDPOSupplierDispatchDTO;
import com.example.master.Dto.DispatchDetailDTO;
import com.example.master.config.TokenHelper;
import com.example.master.dtobj.AcceptCDPO;
import com.example.master.dtobj.AcceptSupervisor;
import com.example.master.entity.AwcCenterr;
import com.example.master.entity.Project;
import com.example.master.entity.UserMetadata;
import com.example.master.model.AWCDispatch;
import com.example.master.model.CDPOSupplierDispatch;
import com.example.master.model.Demand;
import com.example.master.model.DispatchDetail;
import com.example.master.repository.*;
import com.example.master.service.DemandFlowService;
import com.example.master.services.DemandService;
import com.example.master.services.DispatchDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/demand-dispatches")
@Tag(name = "FMS RJ-DEMAND-FLOW", description = "APIs to control demand flow for cdpo and supervisor")
public class DispatchDetailController {

    @Autowired
    private DemandFlowService demandFlowService;
    @PostMapping("/bulk")
    public ResponseEntity<List<DispatchDetail>> createBulk(@RequestBody List<DispatchDetailDTO> dtos) {
        return demandFlowService.createBulk(dtos);
    }

    @GetMapping("/by-demand/{demandId}")
    public ResponseEntity<List<DispatchDetail>> listByDemand(@PathVariable Long demandId){
        return demandFlowService.listByDemand(demandId);
    }

    @GetMapping("/by-demand-awc/{demandId}")
    public ResponseEntity<List<DispatchDetail>> listByDemandAWC(@PathVariable Long demandId){
        return demandFlowService.listByDemandAWC(demandId);
    }

    @PostMapping("/accept")
    public ResponseEntity<DispatchDetail> accept(@RequestBody AcceptCDPO acceptCDPO){
        return demandFlowService.accept(acceptCDPO.getId(), acceptCDPO.getAcceptedPackets(), acceptCDPO.getRemarks());
    }

    @PostMapping("/accept-awc")
    public ResponseEntity<DispatchDetail> acceptAWC(@RequestBody AcceptSupervisor acceptSupervisor){

        return demandFlowService.acceptAWC(acceptSupervisor.getId(), acceptSupervisor.getDispatchId(), acceptSupervisor.getAcceptedPackets(), acceptSupervisor.getRemarks());
    }


    @PostMapping("/dispatch-to-sector")
    public ResponseEntity<List<CDPOSupplierDispatch>> dispatch(@RequestBody List<CDPOSupplierDispatchDTO> dtos) {
        return demandFlowService.dispatch(dtos);
    }

    @PostMapping("/dispatch-to-awc")
    public ResponseEntity<CDPOSupplierDispatch> dispatchAWC(@RequestBody List<AWCDispatchDTO> dtos) {

        return demandFlowService.dispatchAWC(dtos);
    }

    @GetMapping("/qr-file/{id}/{dispatchId}")
    @Operation(summary = "Fetch QR file by demand id and dispatchId")
    public ResponseEntity<Resource> serveQRFile(@PathVariable Long id, @PathVariable("dispatchId") Long dispatchId) {
        try {
            Resource resource = demandFlowService.getDispatchedQR(id, dispatchId);

            String contentType = Files.probeContentType(resource.getFile().toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
