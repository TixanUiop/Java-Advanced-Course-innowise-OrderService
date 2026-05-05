package com.evgeny.orderservice.mapper.order;


import com.evgeny.orderservice.dto.order.CreateOrderDTO;
import com.evgeny.orderservice.dto.order.FullOrderDTO;
import com.evgeny.orderservice.dto.order.OrderItemsDTO;
import com.evgeny.orderservice.dto.order.OrdersSummaryDTO;
import com.evgeny.orderservice.entity.OrderItemsEntity;
import com.evgeny.orderservice.entity.OrdersEntity;
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
