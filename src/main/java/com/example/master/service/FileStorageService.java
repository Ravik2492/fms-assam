package com.example.master.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class FileStorageService {

    @Value("${fms.file.location}")
    private String storagePath;
    private Path storageLocation;// = Paths.get(storagePath);

    public FileStorageService(@Value("${fms.file.location}") String storagePath) throws IOException {
        this.storageLocation = Paths.get(storagePath).normalize();
        Files.createDirectories(this.storageLocation);
    }

    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty file upload not allowed");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String timestampedName = System.currentTimeMillis() + "_" + originalFilename;
        Path targetPath = storageLocation.resolve(timestampedName).normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + originalFilename, e);
        }

        return targetPath.toString(); // Or return relative path if preferred
    }

    public Resource loadFile(String filePathStr) {
        try {
            Path filePath = Paths.get(filePathStr).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new FileNotFoundException("File not found or unreadable: " + filePathStr);
            }

            return resource;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + filePathStr, e);
        }
    }
}

