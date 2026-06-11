package com.enigma.projectmtndew.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScannedReceiptExpenseRequestDTO {

    private UUID groupId;
    private UUID paidBy;
    private String description;
    private float total;
    private float subTotal;

    private List<ScannedItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScannedItem {
        private String name;
        private float price;
        private int quantity;
        private List<UUID> sharedBy;
    }
}