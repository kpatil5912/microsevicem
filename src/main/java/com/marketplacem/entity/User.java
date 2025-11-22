package com.marketplacem.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    // External ID from OAuth provider (like Auth0, Google, etc.)
    private String externalId;

    // Other user fields
    private String firstName;
    private String lastName;
    private boolean active = true;
}
