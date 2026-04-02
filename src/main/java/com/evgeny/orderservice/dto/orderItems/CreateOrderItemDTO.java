package com.evgeny.orderservice.dto.orderItems;
import com.evgeny.orderservice.dto.item.FullItemsDTO;
import com.evgeny.orderservice.dto.order.FullOrderDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderItemDTO {

    private Long orderId;
    private Long itemId;
    private Long quantity;

}
