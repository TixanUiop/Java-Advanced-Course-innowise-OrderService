package com.evgeny.orderservice.service;

import com.evgeny.orderservice.dto.orderItems.CreateOrderItemDTO;
import com.evgeny.orderservice.dto.orderItems.FullOrderItemDTO;

import java.util.List;


public interface OrderItemsService {

    FullOrderItemDTO create(CreateOrderItemDTO item);

    FullOrderItemDTO getById(Long id);

    List<FullOrderItemDTO> getAll();

    FullOrderItemDTO update(Long id, FullOrderItemDTO item);

    void delete(Long id);
}
