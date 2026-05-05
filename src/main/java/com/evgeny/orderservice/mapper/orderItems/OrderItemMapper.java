package com.evgeny.orderservice.mapper.orderItems;


import com.evgeny.orderservice.dto.orderItems.FullOrderItemDTO;
import com.evgeny.orderservice.entity.OrderItemsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "price", source = "item.price")
    @Mapping(target = "productName", source = "item.name")
    @Mapping(target = "deleted", source = "deleted")
    FullOrderItemDTO toDto(OrderItemsEntity entity);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "item", ignore = true)
    OrderItemsEntity toEntity(FullOrderItemDTO dto);

}
