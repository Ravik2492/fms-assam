package com.example.master.dtobj;

import lombok.Data;

@Data
public class CreateUserRequest {
    //private String username;
    private String userid;
    private String email;
    private String firstName;
    private String mobile;
    private String lastName;
    private String password; // optional; set via setPassword after create
    //private boolean temporaryPassword;
    private Role role;
    private UserMetadataRequest userMetadataRequest;
}
