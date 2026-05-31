package com.enigma.projectmtndew.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class SettlementRequestDTO {
    private UUID groupId;
    private UUID fromUser;
    private UUID toUser;
    private float amount;
}