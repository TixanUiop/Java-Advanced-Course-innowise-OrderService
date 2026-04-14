package com.evgeny.orderservice.service;

import com.evgeny.orderservice.dto.item.CreateItemsDTO;
import com.evgeny.orderservice.dto.item.FullItemsDTO;
import com.evgeny.orderservice.entity.ItemsEntity;
import com.evgeny.orderservice.exception.InvalidProductException;
import com.evgeny.orderservice.exception.ProductNotFoundException;
import com.evgeny.orderservice.exception.InvalidProductException;
import com.evgeny.orderservice.mapper.item.ItemMapper;
import com.evgeny.orderservice.repository.ItemsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemsServiceImpl implements ItemsService {

    private final ItemsRepository itemsRepository;
    private final ItemMapper itemMapper;

    @Override
    public FullItemsDTO getById(Long id) {

        ItemsEntity itemsEntity = itemsRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return itemMapper.toFullItemsDTOFromItemsEntity(itemsEntity);
    }

    @Override
    public FullItemsDTO create(CreateItemsDTO create) {

        if (itemsRepository.existsByName(create.getName())) {
            throw new InvalidProductException("Item already exists: " + create.getName());
        }
        ItemsEntity itemsEntityCreateItemsDTO = itemMapper.toItemsEntityCreateItemsDTO(create);
        itemsEntityCreateItemsDTO.setDeleted(false);
        ItemsEntity save = itemsRepository.save(itemsEntityCreateItemsDTO);
        return itemMapper.toFullItemsDTOFromItemsEntity(save);
    }

    @Override
    public List<FullItemsDTO> getAll() {
        List<ItemsEntity> all = itemsRepository.findAll();
        return all.stream().map(itemMapper::toFullItemsDTOFromItemsEntity).toList();
    }

    @Transactional
    @Override
    public FullItemsDTO update(Long id, FullItemsDTO item) {

        ItemsEntity byId = itemsRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        byId.setName(item.getName());
        byId.setPrice(item.getPrice());

        itemsRepository.save(byId);


        return itemMapper.toFullItemsDTOFromItemsEntity(byId);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        ItemsEntity item = itemsRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        item.setDeleted(true);

        itemsRepository.save(item);
    }
}
