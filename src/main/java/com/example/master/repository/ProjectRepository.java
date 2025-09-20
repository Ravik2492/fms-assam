package com.example.master.repository;

import com.example.master.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByProjectCode(String projectCode);

    List<Project> findByDistrictIdIn(List<Long> districtIds);

    Long countByDistrictId(Long districtId);

}

