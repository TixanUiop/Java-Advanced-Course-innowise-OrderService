package com.evgeny.orderservice.controller;

import com.evgeny.orderservice.dto.order.CreateOrderDTO;
import com.evgeny.orderservice.dto.order.FullOrderDTO;
import com.evgeny.orderservice.dto.order.OrdersSummaryDTO;
import com.evgeny.orderservice.entity.Enums.OrderStatus;
import com.evgeny.orderservice.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrdersController {

    private final OrderService ordersService;

    public OrdersController(@Qualifier("orderServiceImpl") OrderService ordersService) {
        this.ordersService = ordersService;
    }

    @PreAuthorize("hasRole('ADMIN') or principal.id == #order.userId")
    @PostMapping("/create")
    public FullOrderDTO createOrder(@Valid @RequestBody CreateOrderDTO order) {
        return ordersService.createOrder(order);
    }

    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOrderOwner(#id, principal.id)")
    @GetMapping("/{id}")
    public FullOrderDTO getOrderById(@PathVariable @Positive Long id) {
        return ordersService.getOrderById(id);
    }

    @PreAuthorize("hasRole('ADMIN') or principal.id == #userId")
    @GetMapping("/user/{userId}")
    public List<OrdersSummaryDTO> getOrdersByUserId(@PathVariable @Positive Long userId) {
        return ordersService.getOrdersByUserId(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<FullOrderDTO> getOrdersFiltered(
            @RequestParam(required = false) List<OrderStatus> statuses,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        LocalDateTime fromDate = from != null ? LocalDateTime.parse(from) : null;
        LocalDateTime toDate = to != null ? LocalDateTime.parse(to) : null;

        return ordersService.getOrdersFiltered(statuses, fromDate, toDate, page, size);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public FullOrderDTO updateOrder(@PathVariable Long id, @Valid @RequestBody FullOrderDTO updatedOrder) {
        return ordersService.updateOrder(id, updatedOrder);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public void softDeleteOrder(@PathVariable @Positive Long id) {
        ordersService.softDeleteOrder(id);
    }
}
