package com.example.master.dtobj;

import java.util.HashMap;
import java.util.Map;

public class PublicDemandResponse {
    private Map<String, Map<String, Object>> details;

    public PublicDemandResponse() {
        this.details = new HashMap<>();
    }

    public void addSection(String sectionName, Map<String, Object> sectionDetails) {
        this.details.put(sectionName, sectionDetails);
    }

    public Map<String, Map<String, Object>> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Map<String, Object>> details) {
        this.details = details;
    }
}

