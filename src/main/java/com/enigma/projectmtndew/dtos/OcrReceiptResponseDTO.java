package com.enigma.projectmtndew.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OcrReceiptResponseDTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OcrReceiptItemDTO{
        private String name;
        private float price;
        private int quantity;
    }

    private List<OcrReceiptItemDTO> items;
    private float subTotal;
    private float tax;
    private float serviceCharge;
    private float total;
}
