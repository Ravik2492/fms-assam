package com.example.master.repository;

import com.example.master.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectorRepository extends JpaRepository<Sector, Long> {

    List<Sector> findByDistrictIdIn(List<Long> districtIds);

    List<Sector> findByProjectIdIn(List<Long> projectIds);

    Long countByDistrictId(Long districtId);

}

