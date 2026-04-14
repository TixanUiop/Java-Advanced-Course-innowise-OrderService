package com.evgeny.orderservice.dto.orderItems;

import com.evgeny.orderservice.dto.item.FullItemsDTO;
import com.evgeny.orderservice.dto.order.FullOrderDTO;
import com.evgeny.orderservice.dto.user.UserDTO;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FullOrderItemDTO {

    private Long id;

    private Long orderId;

    private Long itemId;

    @Positive(message = "Quantity must be greater than 0")
    private Long quantity;

    private BigDecimal price;

    private String productName;

    private Boolean deleted;

    private UserDTO user;
}
