package com.enigma.projectmtndew.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SettlementDTO {
    private UUID id;
    private UUID groupId;
    private UUID paidBy;
    private UUID paidTo;
    private float amount;
    private LocalDateTime settledAt;
}

