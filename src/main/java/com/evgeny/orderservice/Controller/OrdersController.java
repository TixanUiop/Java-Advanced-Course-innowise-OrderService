package com.evgeny.orderservice.Controller;

import com.evgeny.orderservice.DTO.order.CreateOrderDTO;
import com.evgeny.orderservice.DTO.order.FullOrderDTO;
import com.evgeny.orderservice.DTO.order.OrdersSummaryDTO;
import com.evgeny.orderservice.Entity.Enums.OrderStatus;
import com.evgeny.orderservice.Entity.OrdersEntity;
import com.evgeny.orderservice.Service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private final OrderService ordersService;

    public OrdersController(@Qualifier("orderServiceImpl") OrderService ordersService) {
        this.ordersService = ordersService;
    }

    @PostMapping("/create")
    public FullOrderDTO createOrder(@RequestBody CreateOrderDTO order) {
        return ordersService.createOrder(order);
    }

    @GetMapping("/{id}")
    public FullOrderDTO getOrderById(@PathVariable Long id) {
        return ordersService.getOrderById(id);
    }

    @GetMapping("/user/{userId}")
    public List<OrdersSummaryDTO> getOrdersByUserId(@PathVariable Long userId) {
        return ordersService.getOrdersByUserId(userId);
    }

    @GetMapping
    public Page<FullOrderDTO> getOrdersFiltered(
            @RequestParam(required = false) List<OrderStatus> statuses,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        LocalDateTime fromDate = from != null ? LocalDateTime.parse(from) : null;
        LocalDateTime toDate = to != null ? LocalDateTime.parse(to) : null;

        return ordersService.getOrdersFiltered(statuses, fromDate, toDate, page, size);
    }

    @PutMapping("/update/{id}")
    public FullOrderDTO updateOrder(@PathVariable Long id, @RequestBody FullOrderDTO updatedOrder) {
        return ordersService.updateOrder(id, updatedOrder);
    }

    @DeleteMapping("/delete/{id}")
    public void softDeleteOrder(@PathVariable Long id) {
        ordersService.softDeleteOrder(id);
    }
}
