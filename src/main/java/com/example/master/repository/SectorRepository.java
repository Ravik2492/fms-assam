package com.example.master.repository;

import com.example.master.entity.Sectorr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectorRepository extends JpaRepository<Sectorr, Long> {

    List<Sectorr> findByDistrictIdIn(List<Long> districtIds);

    List<Sectorr> findByProjectIdIn(List<Long> projectIds);

    Long countByDistrictId(Long districtId);

}

