package com.example.master.Dto;

public class SupplierDTO {

    private String id;
    private String name;
    private String email;
    private String keycloakUserId;

    private String password;
    private String role;
    private String firstName;
    private String lastName;

    // Constructors
    public SupplierDTO() {}

    public SupplierDTO(String id, String name, String email, String keycloakUserId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.keycloakUserId = keycloakUserId;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getKeycloakUserId() {
        return keycloakUserId;
    }

    public void setKeycloakUserId(String keycloakUserId) {
        this.keycloakUserId = keycloakUserId;
    }
}

