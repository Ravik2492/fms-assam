package com.example.master.repository;

import com.example.master.entity.AwcCenterr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AwcCenterRepository extends JpaRepository<AwcCenterr, Long> {
    boolean existsByCenterId(String centerId);
    Optional<AwcCenterr> findByCenterId(String centerId);

    @Query("SELECT a FROM AwcCenter a WHERE "
            + "(:districtIds IS NULL OR a.district.id IN :districtIds) AND "
            + "(:sectorIds IS NULL OR a.sector.id IN :sectorIds) AND "
            + "(:projectIds IS NULL OR a.project.id IN :projectIds)")
    List<AwcCenterr> findByFilters(
            @Param("districtIds") List<Long> districtIds,
            @Param("sectorIds") List<Long> sectorIds,
            @Param("projectIds") List<Long> projectIds);


}

