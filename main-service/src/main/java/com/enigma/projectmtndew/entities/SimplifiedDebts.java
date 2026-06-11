package com.enigma.projectmtndew.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name="simplified_debts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimplifiedDebts {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID groupId;
    private UUID fromUserId;
    private UUID toUserId;

    private float amount;

    private boolean isSettled;
}
