package com.example.master.controller;


import com.example.master.entity.SiteIdentity;
import com.example.master.service.SiteIdentityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;

@RestController
@RequestMapping("/api/site-identity")
@Tag(name = "FMS RJ-SI", description = "APIs to control site attributes")
public class SiteIdentityController {

    private final SiteIdentityService service;

    public SiteIdentityController(SiteIdentityService service) {
        this.service = service;
    }

    @PreAuthorize("hasAnyRole('ADMIN') or hasAuthority('ROLE_MANAGE_USERS')")
    @PutMapping("/{id}")
    @Operation(summary = "Update site attributes")
    public SiteIdentity updateSiteIdentity(
            @PathVariable Long id,
            @RequestPart("data") SiteIdentity updated,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @RequestPart(value = "loginLogo", required = false) MultipartFile loginLogo
    ) {
        return service.updateSiteIdentity(id, updated, logo, loginLogo);
    }

    @PreAuthorize("hasAnyRole('ADMIN','AWC','NODAL','SUPPLIER') or hasAuthority('ROLE_MANAGE_USERS')")
    @GetMapping("/{id}")
    @Operation(summary = "Fetch site attributes")
    public SiteIdentity getSiteIdentity(@PathVariable Long id) {
        return service.getSiteIdentity(id).orElseThrow(() -> new RuntimeException("Not found"));
    }

    @GetMapping("/logo/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AWC','NODAL','SUPPLIER') or hasAuthority('ROLE_MANAGE_USERS')")
    @Operation(summary = "Fetch site logo (for Login logo pass parameter: login_logo)")
    public ResponseEntity<Resource> serveFile(@PathVariable Long id, @RequestParam(required = false) String logoType) {
        try {
            Resource resource = service.loadFileById(id, logoType);
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

