package com.example.master.dtobj;

import lombok.Data;

@Data
public class KeycloakUserUpdateRequest {
    private String email;
    private String firstName;
    private String lastName;

    private UserMetadataRequest userMetadataRequest;

    // Getters and setters
}

