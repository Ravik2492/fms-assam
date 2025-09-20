package com.example.master.repository;

import com.example.master.entity.UserMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMetadataRepository extends JpaRepository<UserMetadata, String> {
    // Optional: add custom queries if needed
}

