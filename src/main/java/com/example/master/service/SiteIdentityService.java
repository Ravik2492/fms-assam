package com.example.master.service;

import com.example.master.entity.SiteIdentityy;
import com.example.master.repository.SiteIdentityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class SiteIdentityService {

    private final SiteIdentityRepository repository;

    @Autowired
    private FileStorageService fileStorageService;

    public SiteIdentityService(SiteIdentityRepository repository) {
        this.repository = repository;
    }

    public SiteIdentityy updateSiteIdentity(Long id, SiteIdentityy updated, MultipartFile logo, MultipartFile loginLogo) {
        SiteIdentityy existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Site identity not found"));

        existing.setSiteAddress(updated.getSiteAddress());
        existing.setSiteHeaderName(updated.getSiteHeaderName());
        existing.setStatus(updated.getStatus());

        if (logo != null && !logo.isEmpty()) {
            // Save logo file and set path
            String logoPath = saveFile(logo);
            existing.setLogoPath(logoPath);
        }

        if (loginLogo != null && !loginLogo.isEmpty()) {
            // Save login logo file and set path
            String loginLogoPath = saveFile(loginLogo);
            existing.setLoginLogoPath(loginLogoPath);
        }

        return repository.save(existing);
    }

    private String saveFile(MultipartFile file) {
        // Implement file saving logic (e.g., to local storage or cloud)
        // Return the path or URL
        return fileStorageService.storeFile(file);
        //return "/uploads/" + file.getOriginalFilename(); // Example placeholder
    }

    public Optional<SiteIdentityy> getSiteIdentity(Long id) {
        return repository.findById(id);
    }

    public Resource loadFileById(Long id, String logoType) {
        SiteIdentityy identity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SiteIdentity not found"));

        String filePath = identity.getLogoPath();
        if(logoType!=null && logoType.equals("login_logo")) {
            filePath = identity.getLoginLogoPath();
        }
        return fileStorageService.loadFile(filePath);
    }
}

