package com.example.master.controller;


import com.example.master.model.DemandCategory;
import com.example.master.repository.DemandCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UtilityController {

    @Autowired
    DemandCategoryRepository demandCategoryRepository;

    @GetMapping("/demand-categories")
    public List<DemandCategory> getAllDemandCategories() {
        return demandCategoryRepository.findAll();
    }

}
