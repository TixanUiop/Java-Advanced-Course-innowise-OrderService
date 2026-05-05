package com.evgeny.orderservice.controller;


import com.evgeny.orderservice.dto.item.CreateItemsDTO;
import com.evgeny.orderservice.dto.item.FullItemsDTO;
import com.evgeny.orderservice.service.ItemsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<FullItemsDTO> getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(itemsService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<FullItemsDTO> create(@Valid @RequestBody CreateItemsDTO item) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemsService.create(item));
    }

    @GetMapping("/all")
    public ResponseEntity<List<FullItemsDTO>> getAll() {
        return ResponseEntity.ok(itemsService.getAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<FullItemsDTO> update(
            @PathVariable @Positive Long id,
            @RequestBody @Valid FullItemsDTO item) {
        return ResponseEntity.ok(itemsService.update(id, item));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        itemsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
