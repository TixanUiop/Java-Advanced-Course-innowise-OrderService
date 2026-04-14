package com.evgeny.orderservice.unit.service;

import com.evgeny.orderservice.dto.order.CreateOrderDTO;
import com.evgeny.orderservice.dto.order.CreateOrderItemDTO;
import com.evgeny.orderservice.dto.order.FullOrderDTO;
import com.evgeny.orderservice.dto.order.OrdersSummaryDTO;
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
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.mockito.ArgumentMatchers;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Test
    void getOrdersByUserIdShouldReturnOrdersWithUser() {

        OrdersEntity order = new OrdersEntity();
        order.setUserEmail("user@mail.com");

        when(ordersRepository.findByUserId(1L))
                .thenReturn(List.of(order));

        OrdersSummaryDTO summary = new OrdersSummaryDTO();

        when(orderMapper.toOrdersSummaryDTOFromEntity(order))
                .thenReturn(summary);

        UserDTO user = new UserDTO();
        user.setEmail("user@mail.com");

        when(userClientService.getUserByEmail("user@mail.com"))
                .thenReturn(user);

        List<OrdersSummaryDTO> result = orderService.getOrdersByUserId(1L);

        assertEquals(1, result.size());
        assertEquals(user, result.get(0).getUser());

        verify(ordersRepository).findByUserId(1L);
        verify(userClientService).getUserByEmail("user@mail.com");
    }

    @Test
    void getOrdersFilteredShouldReturnPageOfOrders() {
        OrdersEntity order = OrdersEntity.builder()
                .id(1L)
                .userId(1L)
                .userEmail("test@mail.com")
                .status(OrderStatus.Accepted)
                .totalPrice(BigDecimal.valueOf(100))
                .deleted(false)
                .orderItems(List.of())
                .build();

        Page<OrdersEntity> page = new PageImpl<>(List.of(order));

        when(ordersRepository.findAll(
                ArgumentMatchers.<Specification<OrdersEntity>>any(),
                ArgumentMatchers.<Pageable>any()
        )).thenReturn(page);

        FullOrderDTO dto = FullOrderDTO.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.Accepted)
                .totalPrice(BigDecimal.valueOf(100))
                .deleted(false)
                .orderItems(List.of())
                .build();

        when(orderMapper.toFullOrderDTOFromEntity(ArgumentMatchers.<OrdersEntity>any()))
                .thenReturn(dto);

        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setEmail("test@mail.com");

        when(userClientService.getUserByEmail(ArgumentMatchers.<String>any()))
                .thenReturn(user);

        Page<FullOrderDTO> result = orderService.getOrdersFiltered(
                List.of(OrderStatus.Accepted),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                0,
                10
        );

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(user.getEmail(), result.getContent().get(0).getUser().getEmail());

        verify(ordersRepository).findAll(
                ArgumentMatchers.<Specification<OrdersEntity>>any(),
                ArgumentMatchers.<Pageable>any()
        );
        verify(userClientService).getUserByEmail(ArgumentMatchers.<String>any());
    }

}