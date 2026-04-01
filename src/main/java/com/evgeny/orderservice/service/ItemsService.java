package com.evgeny.orderservice.service;

import com.evgeny.orderservice.dto.item.CreateItemsDTO;
import com.evgeny.orderservice.dto.item.FullItemsDTO;
import com.evgeny.orderservice.entity.ItemsEntity;

import java.util.List;

public interface ItemsService {

    FullItemsDTO create(CreateItemsDTO item);

    FullItemsDTO getById(Long id);

    List<FullItemsDTO> getAll();

    FullItemsDTO update(Long id, FullItemsDTO item);

    void delete(Long id);
}
