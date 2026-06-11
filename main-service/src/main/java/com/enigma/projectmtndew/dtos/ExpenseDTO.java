package com.enigma.projectmtndew.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {

    public enum SplitType {
        EQUAL, PERCENTAGE, OCR
    }

    private UUID id;
    private UUID groupId;
    private UUID paidBy;
    private float amount;
    private String description;
    private LocalDateTime addedAt;
    private SplitType splitType;
    private Float subtotal;

    private List<ExpenseSplitDTO> splits;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseSplitDTO {
        private UUID id;
        private Float percent; // PERCENTAGE split only
        private Float amount;
    }
}
