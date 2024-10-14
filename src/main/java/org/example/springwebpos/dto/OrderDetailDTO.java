package org.example.springwebpos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderDetailDTO implements SuperDTO {
    private String itemCode;
    private double unitPrice;
    private int quantity;
    private double totalPrice;
}
