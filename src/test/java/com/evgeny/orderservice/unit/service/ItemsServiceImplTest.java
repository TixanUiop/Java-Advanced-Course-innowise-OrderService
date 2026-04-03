package com.evgeny.orderservice.unit.service;

import com.evgeny.orderservice.dto.item.CreateItemsDTO;
import com.evgeny.orderservice.dto.item.FullItemsDTO;
import com.evgeny.orderservice.entity.ItemsEntity;
import com.evgeny.orderservice.exception.ProductNotFoundException;
import com.evgeny.orderservice.exception.invalidProductException;
import com.evgeny.orderservice.mapper.item.ItemMapper;
import com.evgeny.orderservice.repository.ItemsRepository;
import com.evgeny.orderservice.service.ItemsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemsServiceImplTest {

    @Mock
    private ItemsRepository itemsRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemsServiceImpl itemsService;


    @Test
    void getByIdShouldReturnItem() {
        ItemsEntity entity = new ItemsEntity();
        entity.setId(1L);

        FullItemsDTO dto = new FullItemsDTO();

        when(itemsRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(itemMapper.toFullItemsDTOFromItemsEntity(entity)).thenReturn(dto);

        FullItemsDTO result = itemsService.getById(1L);

        assertNotNull(result);
        verify(itemsRepository).findById(1L);
        verify(itemMapper).toFullItemsDTOFromItemsEntity(entity);
    }

    @Test
    void getByIdShouldThrowIfNotFound() {
        when(itemsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> itemsService.getById(1L));
    }


    @Test
    void createShouldSaveItem() {
        CreateItemsDTO create = new CreateItemsDTO();
        create.setName("item");

        ItemsEntity entity = new ItemsEntity();
        FullItemsDTO dto = new FullItemsDTO();

        when(itemsRepository.existsByName("item")).thenReturn(false);
        when(itemMapper.toItemsEntityCreateItemsDTO(create)).thenReturn(entity);
        when(itemsRepository.save(entity)).thenReturn(entity);
        when(itemMapper.toFullItemsDTOFromItemsEntity(entity)).thenReturn(dto);

        FullItemsDTO result = itemsService.create(create);

        assertNotNull(result);
        verify(itemsRepository).save(entity);
    }

    @Test
    void createShouldThrowIfExists() {
        CreateItemsDTO create = new CreateItemsDTO();
        create.setName("item");

        when(itemsRepository.existsByName("item")).thenReturn(true);

        assertThrows(invalidProductException.class,
                () -> itemsService.create(create));
    }


    @Test
    void getAllShouldReturnList() {
        ItemsEntity entity = new ItemsEntity();
        FullItemsDTO dto = new FullItemsDTO();

        when(itemsRepository.findAll()).thenReturn(List.of(entity));
        when(itemMapper.toFullItemsDTOFromItemsEntity(entity)).thenReturn(dto);

        List<FullItemsDTO> result = itemsService.getAll();

        assertEquals(1, result.size());
    }


    @Test
    void updateShouldUpdateItem() {
        ItemsEntity entity = new ItemsEntity();
        entity.setId(1L);

        FullItemsDTO update = new FullItemsDTO();
        update.setName("new");
        update.setPrice(BigDecimal.TEN);

        FullItemsDTO mapped = new FullItemsDTO();

        when(itemsRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(itemMapper.toFullItemsDTOFromItemsEntity(entity)).thenReturn(mapped);

        FullItemsDTO result = itemsService.update(1L, update);

        assertEquals("new", entity.getName());
        assertEquals(BigDecimal.TEN, entity.getPrice());
        verify(itemsRepository).save(entity);
    }

    @Test
    void updateShouldThrowIfNotFound() {
        when(itemsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> itemsService.update(1L, new FullItemsDTO()));
    }


    @Test
    void deleteShouldDelete() {
        when(itemsRepository.existsById(1L)).thenReturn(true);

        itemsService.delete(1L);

        verify(itemsRepository).deleteById(1L);
    }

    @Test
    void deleteShouldThrowIfNotFound() {
        when(itemsRepository.existsById(1L)).thenReturn(false);

        assertThrows(ProductNotFoundException.class,
                () -> itemsService.delete(1L));
    }
}