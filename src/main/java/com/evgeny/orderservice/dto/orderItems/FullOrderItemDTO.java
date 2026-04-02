package com.evgeny.orderservice.dto.orderItems;

import com.evgeny.orderservice.dto.item.FullItemsDTO;
import com.evgeny.orderservice.dto.order.FullOrderDTO;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FullOrderItemDTO {

    private Long id;

    //private FullOrderDTO order;
    private Long orderId;

    //private FullItemsDTO item;
    private Long itemId;

    @Positive
    private Long quantity;

    private BigDecimal price;

    private String productName;

    private Boolean deleted;
}
