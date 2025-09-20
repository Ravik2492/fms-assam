package com.example.master.repository;

import com.example.master.model.Cdpos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CdpoRepository extends JpaRepository<Cdpos, Long> {
    List<Cdpos> findByDistrictId(Long districtId);
}
