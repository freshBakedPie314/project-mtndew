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
    private UUID id;
    private String description;
    private LocalDateTime addedAt;
    private float amount;
    private UUID paidBy;
    private UUID groupId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseSplitDTO {
        private UUID id;
        private String username;
        private Float amount;
    }

    private List<ExpenseSplitDTO> splits;
}
