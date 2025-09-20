package com.example.master.repository;

import com.example.master.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictRepository extends JpaRepository<District, Long> {
    boolean existsByDistrictName(String districtName);
}

