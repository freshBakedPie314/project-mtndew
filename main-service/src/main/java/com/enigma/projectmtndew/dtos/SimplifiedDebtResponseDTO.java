package com.enigma.projectmtndew.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimplifiedDebtResponseDTO {
    public UUID fromUserId;
    public UUID toUserId;
    public Float amount;
}
