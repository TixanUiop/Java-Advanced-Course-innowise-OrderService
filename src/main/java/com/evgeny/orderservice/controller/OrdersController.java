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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<FullOrderDTO> createOrder(@Valid @RequestBody CreateOrderDTO order) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ordersService.createOrder(order));
    }

    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOrderOwner(#id, principal.id)")
    @GetMapping("/{id}")
    public ResponseEntity<FullOrderDTO> getOrderById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ordersService.getOrderById(id));
    }

    @PreAuthorize("hasRole('ADMIN') or principal.id == #userId")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrdersSummaryDTO>> getOrdersByUserId(@PathVariable @Positive Long userId) {
        return ResponseEntity.ok(ordersService.getOrdersByUserId(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<FullOrderDTO>> getOrdersFiltered(
            @RequestParam(required = false) List<OrderStatus> statuses,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        LocalDateTime fromDate = from != null ? LocalDateTime.parse(from) : null;
        LocalDateTime toDate = to != null ? LocalDateTime.parse(to) : null;

        Page<FullOrderDTO> result = ordersService.getOrdersFiltered(statuses, fromDate, toDate, page, size);

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<FullOrderDTO> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody FullOrderDTO updatedOrder) {
        return ResponseEntity.ok(ordersService.updateOrder(id, updatedOrder));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> softDeleteOrder(@PathVariable @Positive Long id) {
        ordersService.softDeleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
