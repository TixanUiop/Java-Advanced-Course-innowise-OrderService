package com.evgeny.orderservice.dto.order;

import com.evgeny.orderservice.dto.user.UserDTO;
import com.evgeny.orderservice.entity.Enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdersSummaryDTO {
    private Long id;
    private UserDTO user;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private boolean deleted;
}
