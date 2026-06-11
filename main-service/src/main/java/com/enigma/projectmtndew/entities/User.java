package com.enigma.projectmtndew.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users_info")
@Getter
@Setter
public class User {
    @Id
    UUID id;

    @Column(unique = true, nullable = false)
    String email;

    @Column(nullable = false)
    String username;
}
