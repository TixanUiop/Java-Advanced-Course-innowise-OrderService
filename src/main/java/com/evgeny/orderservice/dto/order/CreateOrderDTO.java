package com.evgeny.orderservice.dto.order;

import com.evgeny.orderservice.entity.Enums.OrderStatus;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreateOrderDTO {

    @NotNull(message = "UserId is required")
    @Positive
    private Long userId;

    private BigDecimal totalPrice;

    @NotNull(message = "Status is required")
    private OrderStatus status;

    private boolean deleted;

    @NotEmpty(message = "Order must contain at least one item")
    private List<CreateOrderItemDTO> orderItems;
}
