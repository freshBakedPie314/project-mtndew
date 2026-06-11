package com.enigma.projectmtndew.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserExpenseSummaryResponseDTO {
    private Float totalOwed;
    private Float totalLent;
}
