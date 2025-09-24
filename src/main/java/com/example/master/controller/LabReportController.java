// LabReportController.java
package com.example.master.controller;

import com.example.master.Dto.LabReportDTO;
import com.example.master.model.LabReport;
import com.example.master.service.FileStorageService;
import com.example.master.service.QRCodeService;
import com.example.master.services.LabReportService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/lab-reports")
public class LabReportController {

    private final LabReportService service;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private QRCodeService qrCodeService;

    public LabReportController(LabReportService service){ this.service = service; }

    @PostMapping
    public ResponseEntity<LabReport> create(@RequestBody LabReportDTO dto, @RequestPart("authorizationDoc") MultipartFile authorizationDoc){
        LabReport lr = new LabReport();
        lr.setDemandId(dto.demandId);
        lr.setLabName(dto.getLabName());
        lr.setManufacturingDate(dto.getManufacturingDate());
        lr.setExpiryDate(dto.getExpiryDate());
        lr.setTestDate(dto.getTestDate());
        lr.setStatus(dto.getStatus());
        lr.setRemarks(dto.getRemarks());
        lr.setFilePath(fileStorageService.storeFile(authorizationDoc));

        String qrUrl = "https://snp-assam.eighteenpixels.com/qr/"+dto.demandId;
        String qrPath = qrCodeService.generate(qrUrl);// need to add url path
        lr.setQrCodePath(qrPath);

        LabReport saved = service.createLabReport(lr);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping("/by-demand/{demandId}")
    public ResponseEntity<List<LabReport>> listByDemand(@PathVariable Long demandId){
        return ResponseEntity.ok(service.findByDemandId(demandId));
    }

    @GetMapping("/file/{id}")
    @Operation(summary = "Fetch lab report file by demand id")
    public ResponseEntity<Resource> serveFile(@PathVariable Long id) {
        try {
            Resource resource = service.loadLabReportById(id);

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

    @GetMapping("/qr-file/{id}")
    @Operation(summary = "Fetch lab report file by demand id")
    public ResponseEntity<Resource> serveQRFile(@PathVariable Long id) {
        try {
            Resource resource = service.loadLabReportQRById(id);

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
