// LabReportService.java
package com.example.master.services;

import com.example.master.model.LabReport;
import org.springframework.core.io.Resource;

import java.util.List;

public interface LabReportService {
    LabReport createLabReport(LabReport labReport);
    List<LabReport> findByDemandId(Long demandId);

    Resource loadLabReportById(Long id);

    Resource loadLabReportQRById(Long id);

}
