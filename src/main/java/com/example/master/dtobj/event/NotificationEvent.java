package com.example.master.dtobj.event;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class NotificationEvent {
    private String templateName; // "demand-create", "supplier-accept", etc.
    private String demandId;
    private List<String> recipients; // AWC user emails
    private Map<String, String> values; // template variables
}

