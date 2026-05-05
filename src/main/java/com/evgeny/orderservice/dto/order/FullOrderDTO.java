package com.evgeny.orderservice.dto.order;


import com.evgeny.orderservice.dto.user.UserDTO;
import com.evgeny.orderservice.entity.Enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Id is required")
    private Long id;

    @NotNull(message = "UserId is required")
    private Long userId;

    private BigDecimal totalPrice;

    @NotNull(message = "Status is required")
    private OrderStatus status;

    private boolean deleted;

    @NotEmpty(message = "Order must contain items")
    private List<@Valid OrderItemsDTO> orderItems;

    private UserDTO user;
}
