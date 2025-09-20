package com.example.master.dtobj;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class KcUser {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private Boolean emailVerified;
    private Long createdTimestamp;
    private Boolean enabled;
    private Boolean totp;
    private Map<String, Object> access;
}


