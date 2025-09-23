// DispatchDetailServiceImpl.java
package com.example.master.services.impl;

import com.example.master.model.DispatchDetail;
import com.example.master.repository.DispatchDetailRepository;
import com.example.master.services.DispatchDetailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DispatchDetailServiceImpl implements DispatchDetailService {

    private final DispatchDetailRepository repo;

    public DispatchDetailServiceImpl(DispatchDetailRepository repo){ this.repo = repo; }

    @Override
    public List<DispatchDetail> createDispatches(List<DispatchDetail> dispatches) {


        return repo.saveAll(dispatches);
    }

    @Override
    public List<DispatchDetail> findByDemandId(Long demandId) {
        return repo.findByDemandId(demandId);
    }
}
