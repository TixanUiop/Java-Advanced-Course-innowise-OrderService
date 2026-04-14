package com.evgeny.orderservice.unit.service;

import com.evgeny.orderservice.dto.order.CreateOrderDTO;
import com.evgeny.orderservice.dto.order.CreateOrderItemDTO;
import com.evgeny.orderservice.dto.order.FullOrderDTO;
import com.evgeny.orderservice.dto.user.UserDTO;
import com.evgeny.orderservice.entity.Enums.OrderStatus;
import com.evgeny.orderservice.entity.ItemsEntity;
import com.evgeny.orderservice.entity.OrdersEntity;
import com.evgeny.orderservice.exception.InvalidOrderException;
import com.evgeny.orderservice.exception.OrderNotFoundException;
import com.evgeny.orderservice.exception.ProductNotFoundException;
import com.evgeny.orderservice.mapper.order.OrderMapper;
import com.evgeny.orderservice.repository.ItemsRepository;
import com.evgeny.orderservice.repository.OrdersRepository;
import com.evgeny.orderservice.service.OrderServiceImpl;
import com.evgeny.orderservice.service.UserClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private ItemsRepository itemsRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserClientService userClientService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private ItemsEntity item;

    @BeforeEach
    void setup() {
        item = ItemsEntity.builder()
                .id(1L)
                .price(BigDecimal.valueOf(100))
                .build();
    }

    @Test
    void createOrderSuccess() {

        CreateOrderItemDTO itemDTO = new CreateOrderItemDTO(1L, 2L);

        CreateOrderDTO dto = CreateOrderDTO.builder()
                .userId(1L)
                .status(OrderStatus.Collect)
                .orderItems(List.of(itemDTO))
                .build();

        when(itemsRepository.findById(1L)).thenReturn(Optional.of(item));

        when(userClientService.getUserByEmail(any()))
                .thenReturn(new UserDTO());

        when(orderMapper.toFullOrderDTOFromEntity(any(OrdersEntity.class)))
                .thenReturn(new FullOrderDTO());

        FullOrderDTO result = orderService.createOrder(dto);

        assertNotNull(result);

        verify(itemsRepository).findById(1L);
        verify(ordersRepository).save(any(OrdersEntity.class));
    }

    @Test
    void createOrderEmptyItemsShouldThrow() {

        CreateOrderDTO dto = CreateOrderDTO.builder()
                .userId(1L)
                .status(OrderStatus.Collect)
                .orderItems(List.of())
                .build();

        assertThrows(InvalidOrderException.class,
                () -> orderService.createOrder(dto));
    }

    @Test
    void createOrderProductNotFoundShouldThrow() {

        CreateOrderItemDTO itemDTO = new CreateOrderItemDTO(1L, 2L);

        CreateOrderDTO dto = CreateOrderDTO.builder()
                .userId(1L)
                .status(OrderStatus.Collect)
                .orderItems(List.of(itemDTO))
                .build();

        when(itemsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> orderService.createOrder(dto));
    }

    @Test
    void createOrderInvalidQuantityShouldThrow() {

        CreateOrderItemDTO itemDTO = new CreateOrderItemDTO(1L, 0L);

        CreateOrderDTO dto = CreateOrderDTO.builder()
                .userId(1L)
                .status(OrderStatus.Collect)
                .orderItems(List.of(itemDTO))
                .build();

        when(itemsRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(InvalidOrderException.class,
                () -> orderService.createOrder(dto));
    }

    @Test
    void getOrderByIdSuccess() {

        OrdersEntity entity = OrdersEntity.builder()
                .id(1L)
                .userId(1L)
                .build();

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(entity));

        when(orderMapper.toFullOrderDTOFromEntity(entity))
                .thenReturn(new FullOrderDTO());

        when(userClientService.getUserByEmail(any()))
                .thenReturn(new UserDTO());

        FullOrderDTO result = orderService.getOrderById(1L);

        assertNotNull(result);

        verify(ordersRepository).findById(1L);
    }

    @Test
    void getOrderById_notFound_shouldThrow() {

        when(ordersRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrderById(1L));
    }

    @Test
    void updateOrderNotFoundShouldThrow() {

        when(ordersRepository.findById(1L)).thenReturn(Optional.empty());

        FullOrderDTO dto = new FullOrderDTO();

        assertThrows(OrderNotFoundException.class,
                () -> orderService.updateOrder(1L, dto));
    }

    @Test
    void updateOrderEmptyItemsShouldThrow() {

        OrdersEntity existing = OrdersEntity.builder()
                .id(1L)
                .build();

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(existing));

        FullOrderDTO dto = FullOrderDTO.builder()
                .orderItems(List.of())
                .build();

        assertThrows(InvalidOrderException.class,
                () -> orderService.updateOrder(1L, dto));
    }

    @Test
    void softDeleteOrderSuccess() {

        OrdersEntity entity = OrdersEntity.builder()
                .id(1L)
                .deleted(false)
                .build();

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(entity));

        orderService.softDeleteOrder(1L);

        verify(ordersRepository).delete(entity);
    }

    @Test
    void softDeleteOrderNotFoundShouldThrow() {

        when(ordersRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.softDeleteOrder(1L));
    }
}