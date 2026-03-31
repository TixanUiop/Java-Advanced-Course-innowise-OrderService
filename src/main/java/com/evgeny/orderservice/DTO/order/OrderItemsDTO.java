package com.evgeny.orderservice.DTO.order;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemsDTO {
    private Long id;
    private Long productId;
    private Integer quantity;
}
