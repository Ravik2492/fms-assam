package com.example.master.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "userss")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Userr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;
}