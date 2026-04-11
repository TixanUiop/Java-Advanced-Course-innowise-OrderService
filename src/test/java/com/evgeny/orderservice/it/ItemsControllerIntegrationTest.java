package com.evgeny.orderservice.it;

import com.evgeny.orderservice.dto.orderItems.CreateOrderItemDTO;
import com.evgeny.orderservice.dto.orderItems.FullOrderItemDTO;
import com.evgeny.orderservice.entity.ItemsEntity;
import com.evgeny.orderservice.entity.OrderItemsEntity;
import com.evgeny.orderservice.entity.OrdersEntity;
import com.evgeny.orderservice.exception.InvalidOrderOperationException;
import com.evgeny.orderservice.exception.ProductNotFoundException;
import com.evgeny.orderservice.mapper.orderItems.OrderItemMapper;
import com.evgeny.orderservice.repository.ItemsRepository;
import com.evgeny.orderservice.repository.OrderItemsRepository;
import com.evgeny.orderservice.repository.OrdersRepository;
import com.evgeny.orderservice.security.JwtUserDetails;
import com.evgeny.orderservice.service.OrderItemsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemsControllerIntegrationTest {

    @Mock
    private OrderItemsRepository orderItemsRepository;

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private ItemsRepository itemsRepository;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderItemsServiceImpl orderItemsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        JwtUserDetails mockUser = mock(JwtUserDetails.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getRole()).thenReturn("USER");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(mockUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createOrderItemSuccess() {

        CreateOrderItemDTO createDTO = CreateOrderItemDTO.builder()
                .orderId(10L)
                .itemId(20L)
                .quantity(2L)
                .build();

        OrdersEntity orderEntity = OrdersEntity.builder()
                .id(10L)
                .userId(1L)
                .build();

        ItemsEntity itemEntity = ItemsEntity.builder()
                .id(20L)
                .price(BigDecimal.valueOf(100))
                .build();

        OrderItemsEntity savedEntity = OrderItemsEntity.builder()
                .order(orderEntity)
                .item(itemEntity)
                .quantity(2L)
                .deleted(false)
                .build();

        FullOrderItemDTO dto = FullOrderItemDTO.builder()
                .orderId(10L)
                .itemId(20L)
                .quantity(2L)
                .price(BigDecimal.valueOf(100))
                .build();

        when(ordersRepository.findById(10L)).thenReturn(Optional.of(orderEntity));
        when(itemsRepository.findById(20L)).thenReturn(Optional.of(itemEntity));
        when(orderItemsRepository.save(any(OrderItemsEntity.class))).thenReturn(savedEntity);
        when(orderItemMapper.toDto(savedEntity)).thenReturn(dto);

        FullOrderItemDTO result = orderItemsService.create(createDTO);

        assertNotNull(result);
        assertEquals(10L, result.getOrderId());
        assertEquals(20L, result.getItemId());
        assertEquals(2L, result.getQuantity());

        verify(orderItemsRepository).save(any(OrderItemsEntity.class));
    }

    @Test
    void createOrderItemInvalidUserThrows() {

        CreateOrderItemDTO createDTO = CreateOrderItemDTO.builder()
                .orderId(10L)
                .itemId(20L)
                .quantity(2L)
                .build();

        OrdersEntity orderEntity = OrdersEntity.builder()
                .id(10L)
                .userId(99L)
                .build();

        when(ordersRepository.findById(10L)).thenReturn(Optional.of(orderEntity));

        assertThrows(InvalidOrderOperationException.class,
                () -> orderItemsService.create(createDTO));
    }

    @Test
    void createOrderItemInvalidProductThrows() {

        CreateOrderItemDTO createDTO = CreateOrderItemDTO.builder()
                .orderId(10L)
                .itemId(20L)
                .quantity(2L)
                .build();

        OrdersEntity orderEntity = OrdersEntity.builder()
                .id(10L)
                .userId(1L)
                .build();

        when(ordersRepository.findById(10L)).thenReturn(Optional.of(orderEntity));
        when(itemsRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> orderItemsService.create(createDTO));
    }

    @Test
    void updateOrderItemSuccess() {

        OrdersEntity orderEntity = OrdersEntity.builder()
                .id(10L)
                .userId(1L)
                .build();

        ItemsEntity itemEntity = ItemsEntity.builder()
                .id(20L)
                .price(BigDecimal.valueOf(100))
                .build();

        OrderItemsEntity existing = OrderItemsEntity.builder()
                .id(1L)
                .order(orderEntity)
                .item(itemEntity)
                .quantity(2L)
                .deleted(false)
                .build();

        FullOrderItemDTO updateDTO = FullOrderItemDTO.builder()
                .id(1L)
                .orderId(10L)
                .itemId(20L)
                .quantity(5L)
                .build();

        when(orderItemsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(itemsRepository.findById(20L)).thenReturn(Optional.of(itemEntity));
        when(ordersRepository.findById(10L)).thenReturn(Optional.of(orderEntity));
        when(orderItemsRepository.save(existing)).thenReturn(existing);
        when(orderItemMapper.toDto(existing)).thenReturn(updateDTO);

        FullOrderItemDTO result = orderItemsService.update(1L, updateDTO);

        assertNotNull(result);
        assertEquals(5L, result.getQuantity());

        verify(orderItemsRepository).save(existing);
    }

    @Test
    void updateOrderItemAccessDeniedThrows() {

        OrdersEntity orderEntity = OrdersEntity.builder()
                .id(10L)
                .userId(99L)
                .build();

        OrderItemsEntity existing = OrderItemsEntity.builder()
                .id(1L)
                .order(orderEntity)
                .quantity(2L)
                .build();

        FullOrderItemDTO updateDTO = FullOrderItemDTO.builder()
                .id(1L)
                .quantity(5L)
                .build();

        when(orderItemsRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class,
                () -> orderItemsService.update(1L, updateDTO));
    }

    @Test
    void deleteOrderItemSuccess() {

        OrdersEntity orderEntity = OrdersEntity.builder()
                .id(10L)
                .userId(1L)
                .build();

        OrderItemsEntity existing = OrderItemsEntity.builder()
                .id(1L)
                .order(orderEntity)
                .deleted(false)
                .build();

        when(orderItemsRepository.findById(1L)).thenReturn(Optional.of(existing));

        orderItemsService.delete(1L);

        verify(orderItemsRepository).delete(existing);
    }

    @Test
    void deleteOrderItemAccessDeniedThrows() {

        OrdersEntity orderEntity = OrdersEntity.builder()
                .id(10L)
                .userId(99L)
                .build();

        OrderItemsEntity existing = OrderItemsEntity.builder()
                .id(1L)
                .order(orderEntity)
                .deleted(false)
                .build();

        when(orderItemsRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(InvalidOrderOperationException.class,
                () -> orderItemsService.delete(1L));
    }
}