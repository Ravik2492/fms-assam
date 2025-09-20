package com.example.master.model;

import jakarta.persistence.*;

@Entity
@Table(name = "supplier")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column( unique = true)
    private String email;

    @Column(nullable = true)
    private String keycloakUserId;  // link with Keycloak

    // Constructors
    public Supplier() {}

    public Supplier(Long id, String name, String email, String keycloakUserId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.keycloakUserId = keycloakUserId;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getKeycloakUserId() { return keycloakUserId; }
    public void setKeycloakUserId(String keycloakUserId) { this.keycloakUserId = keycloakUserId; }
}