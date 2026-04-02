package com.evgeny.orderservice.controller;


import com.evgeny.orderservice.dto.item.CreateItemsDTO;
import com.evgeny.orderservice.dto.item.FullItemsDTO;
import com.evgeny.orderservice.service.ItemsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Validated
public class ItemsController {

    private final ItemsService itemsService;

    @GetMapping("/{id}")
    public FullItemsDTO getById(@PathVariable @Positive Long id) {
        return itemsService.getById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public FullItemsDTO create(@Valid @RequestBody CreateItemsDTO item) {
        return itemsService.create(item);
    }

    @GetMapping("/all")
    public List<FullItemsDTO> getAll() {
        return itemsService.getAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public FullItemsDTO update(@PathVariable @Positive Long id,
                              @RequestBody @Valid FullItemsDTO item) {
        return itemsService.update(id, item);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable @Positive Long id) {
        itemsService.delete(id);
    }
}
