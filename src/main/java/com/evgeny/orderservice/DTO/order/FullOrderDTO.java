package com.evgeny.orderservice.DTO.order;


import com.evgeny.orderservice.Entity.Enums.OrderStatus;
import com.evgeny.orderservice.Entity.OrderItemsEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FullOrderDTO {

    private Long id;

    private Long userId;

    private BigDecimal totalPrice;

    private OrderStatus status;

    private boolean deleted;

    List<OrderItemsEntity> orderItems;
}
