package com.evgeny.orderservice.controller;

import com.evgeny.orderservice.dto.orderItems.CreateOrderItemDTO;
import com.evgeny.orderservice.dto.orderItems.FullOrderItemDTO;
import com.evgeny.orderservice.service.OrderItemsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
public class OrderItemsController {

    private final OrderItemsService orderItemsService;

    @GetMapping("/{id}")
    public FullOrderItemDTO getById(@PathVariable Long id) {
        return orderItemsService.getById(id);
    }

    @PostMapping("/create")
    public FullOrderItemDTO create(@RequestBody CreateOrderItemDTO dto) {
        return orderItemsService.create(dto);
    }

    @GetMapping
    public List<FullOrderItemDTO> getAll() {
        return orderItemsService.getAll();
    }

    @PutMapping("/{id}")
    public FullOrderItemDTO update(@PathVariable Long id, @RequestBody FullOrderItemDTO dto) {
        return orderItemsService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        orderItemsService.delete(id);
    }

}
