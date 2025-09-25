package com.example.master.repository;

import com.example.master.entity.DemandStatuses;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemandStatusesRepository extends JpaRepository<DemandStatuses, Long> {
    //List<DemandStatuses> findByDemandRequestIdOrderByStatusDateDesc(Long demandId);
}

