package com.enigma.projectmtndew.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "expenses")
@Data
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String description;
    private LocalDateTime addedAt;
    private float amount;
    private UUID paidBy;
    private UUID groupId;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExpenseSplit> expenseSplits;

    @PrePersist
    void prePersist()
    {
        addedAt = LocalDateTime.now();
    }
}
