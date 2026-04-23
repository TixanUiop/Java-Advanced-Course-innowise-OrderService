package com.evgeny.orderservice.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEventDTO {
    private String orderId;
    private String userId;
    private String status;
    private BigDecimal amount;
}
