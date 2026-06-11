package com.enigma.projectmtndew.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlements")
@Data
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "paid_by", nullable = false)
    private UUID paidBy;

    @Column(name = "paid_to", nullable = false)
    private UUID paidTo;

    @Column(nullable = false)
    private float amount;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @PrePersist
    public void prePersist() {
        settledAt = LocalDateTime.now();
    }
}