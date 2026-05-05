package com.evgeny.orderservice.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderItemDTO {

    @NotNull(message = "ProductId must not be null")
    private Long productId;

    @NotNull(message = "Quantity must not be null")
    @Positive(message = "Quantity must be greater than 0")
    private Long quantity;
}
