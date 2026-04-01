package com.evgeny.orderservice.Service;

import com.evgeny.orderservice.DTO.order.CreateOrderDTO;
import com.evgeny.orderservice.DTO.order.FullOrderDTO;
import com.evgeny.orderservice.DTO.order.OrdersSummaryDTO;
import com.evgeny.orderservice.Entity.Enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface OrderService {

    FullOrderDTO createOrder(CreateOrderDTO order);
    FullOrderDTO getOrderById(Long id);
    Page<FullOrderDTO> getOrdersFiltered(
            List<OrderStatus> statuses,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    );
    List<OrdersSummaryDTO> getOrdersByUserId(Long userId);
    FullOrderDTO updateOrder(Long id, FullOrderDTO updatedOrder);
    void softDeleteOrder(Long id);
}
