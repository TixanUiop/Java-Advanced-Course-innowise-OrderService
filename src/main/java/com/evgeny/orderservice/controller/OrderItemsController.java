package com.evgeny.orderservice.controller;

import com.evgeny.orderservice.dto.orderItems.CreateOrderItemDTO;
import com.evgeny.orderservice.dto.orderItems.FullOrderItemDTO;
import com.evgeny.orderservice.service.OrderItemsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
public class OrderItemsController {

    private final OrderItemsService orderItemsService;


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<FullOrderItemDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderItemsService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<FullOrderItemDTO> create(@RequestBody CreateOrderItemDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderItemsService.create(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<FullOrderItemDTO>> getAll() {
        return ResponseEntity.ok(orderItemsService.getAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FullOrderItemDTO> update(@PathVariable Long id,
                                                   @RequestBody FullOrderItemDTO dto) {
        return ResponseEntity.ok(orderItemsService.update(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderItemsService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
