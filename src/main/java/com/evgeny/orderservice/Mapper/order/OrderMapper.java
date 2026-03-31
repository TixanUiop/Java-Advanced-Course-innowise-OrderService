package com.evgeny.orderservice.Mapper.order;


import com.evgeny.orderservice.DTO.order.CreateOrderDTO;
import com.evgeny.orderservice.DTO.order.FullOrderDTO;
import com.evgeny.orderservice.DTO.order.OrderItemsDTO;
import com.evgeny.orderservice.DTO.order.OrdersSummaryDTO;
import com.evgeny.orderservice.Entity.OrderItemsEntity;
import com.evgeny.orderservice.Entity.OrdersEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrdersEntity toOrdersEntityFromCreate(CreateOrderDTO dto);

    FullOrderDTO toFullOrderDTOFromEntity(OrdersEntity entity);

    OrdersSummaryDTO toOrdersSummaryDTOFromEntity(OrdersEntity entity);

    OrdersEntity toOrdersEntityFromFullOrderDTO(FullOrderDTO dto);

    @Mapping(target = "productId", source = "item.id")
    OrderItemsDTO toDto(OrderItemsEntity entity);

    List<OrderItemsDTO> toDto(List<OrderItemsEntity> entities);
}
