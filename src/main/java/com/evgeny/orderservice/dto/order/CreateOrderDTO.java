package com.evgeny.orderservice.dto.order;

import com.evgeny.orderservice.entity.Enums.OrderStatus;

import jakarta.validation.constraints.Email;
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
    @Positive(message = "UserId must be positive")
    private Long userId;

    @NotNull(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String userEmail;

    @NotNull(message = "Status is required")
    private OrderStatus status;

    @NotEmpty(message = "Order must contain at least one item")
    private List<CreateOrderItemDTO> orderItems;
}
