package com.evgeny.orderservice.unit.service;

import com.evgeny.orderservice.dto.orderItems.CreateOrderItemDTO;
import com.evgeny.orderservice.dto.orderItems.FullOrderItemDTO;
import com.evgeny.orderservice.entity.ItemsEntity;
import com.evgeny.orderservice.entity.OrderItemsEntity;
import com.evgeny.orderservice.entity.OrdersEntity;
import com.evgeny.orderservice.exception.AccessDeniedException;
import com.evgeny.orderservice.exception.InvalidOrderOperationException;
import com.evgeny.orderservice.exception.OrderItemsNotFoundException;
import com.evgeny.orderservice.exception.ProductNotFoundException;
import com.evgeny.orderservice.mapper.orderItems.OrderItemMapper;
import com.evgeny.orderservice.repository.ItemsRepository;
import com.evgeny.orderservice.repository.OrderItemsRepository;
import com.evgeny.orderservice.repository.OrdersRepository;
import com.evgeny.orderservice.security.JwtUserDetails;
import com.evgeny.orderservice.service.OrderItemsServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderItemsServiceImplTest {

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

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        JwtUserDetails user = mock(JwtUserDetails.class);
        when(user.getId()).thenReturn(1L);
        when(user.getRole()).thenReturn("ADMIN");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        closeable.close();
    }


    @Test
    void getByIdSuccess() {

        OrderItemsEntity entity = OrderItemsEntity.builder()
                .id(1L)
                .build();

        FullOrderItemDTO dto = new FullOrderItemDTO();

        when(orderItemsRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(orderItemMapper.toDto(entity)).thenReturn(dto);

        FullOrderItemDTO result = orderItemsService.getById(1L);

        assertNotNull(result);
    }

    @Test
    void getByIdNotFoundThrows() {

        when(orderItemsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderItemsNotFoundException.class,
                () -> orderItemsService.getById(1L));
    }

    @Test
    void getAllFiltersDeleted() {

        OrderItemsEntity active = OrderItemsEntity.builder()
                .deleted(false)
                .build();

        OrderItemsEntity deleted = OrderItemsEntity.builder()
                .deleted(true)
                .build();

        when(orderItemsRepository.findAll()).thenReturn(List.of(active, deleted));
        when(orderItemMapper.toDto(active)).thenReturn(new FullOrderItemDTO());

        List<FullOrderItemDTO> result = orderItemsService.getAll();

        assertEquals(1, result.size());
    }

    @Test
    void updateSuccess() {

        OrdersEntity order = OrdersEntity.builder()
                .id(10L)
                .userId(1L)
                .build();

        ItemsEntity item = ItemsEntity.builder()
                .id(20L)
                .build();

        OrderItemsEntity entity = OrderItemsEntity.builder()
                .id(1L)
                .order(order)
                .item(item)
                .quantity(1L)
                .build();

        FullOrderItemDTO dto = new FullOrderItemDTO();
        dto.setQuantity(5L);
        dto.setOrderId(10L);
        dto.setItemId(20L);

        when(orderItemsRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(ordersRepository.findById(10L)).thenReturn(Optional.of(order));
        when(itemsRepository.findById(20L)).thenReturn(Optional.of(item));
        when(orderItemMapper.toDto(entity)).thenReturn(new FullOrderItemDTO());

        FullOrderItemDTO result = orderItemsService.update(1L, dto);

        assertNotNull(result);
        assertEquals(5L, entity.getQuantity());
    }

    @Test
    void updateAccessDeniedThrows() {

        JwtUserDetails user = mock(JwtUserDetails.class);
        when(user.getId()).thenReturn(2L);
        when(user.getRole()).thenReturn("USER");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);

        OrdersEntity order = OrdersEntity.builder()
                .userId(1L)
                .build();

        OrderItemsEntity entity = OrderItemsEntity.builder()
                .order(order)
                .build();

        when(orderItemsRepository.findById(1L)).thenReturn(Optional.of(entity));

        FullOrderItemDTO dto = new FullOrderItemDTO();
        dto.setQuantity(5L);

        assertThrows(AccessDeniedException.class,
                () -> orderItemsService.update(1L, dto));
    }

    @Test
    void createOrderItemSuccess() {

        CreateOrderItemDTO createDTO = CreateOrderItemDTO.builder()
                .orderId(10L)
                .itemId(20L)
                .quantity(2L)
                .build();

        OrdersEntity order = OrdersEntity.builder()
                .id(10L)
                .userId(1L)
                .build();

        ItemsEntity item = ItemsEntity.builder()
                .id(20L)
                .price(BigDecimal.valueOf(100))
                .build();

        OrderItemsEntity saved = OrderItemsEntity.builder()
                .order(order)
                .item(item)
                .quantity(2L)
                .deleted(false)
                .build();

        FullOrderItemDTO dto = FullOrderItemDTO.builder()
                .orderId(10L)
                .itemId(20L)
                .quantity(2L)
                .price(BigDecimal.valueOf(100))
                .build();

        when(ordersRepository.findById(10L)).thenReturn(Optional.of(order));
        when(itemsRepository.findById(20L)).thenReturn(Optional.of(item));
        when(orderItemsRepository.save(any())).thenReturn(saved);
        when(orderItemMapper.toDto(saved)).thenReturn(dto);

        FullOrderItemDTO result = orderItemsService.create(createDTO);

        assertNotNull(result);
        assertEquals(10L, result.getOrderId());
    }

    @Test
    void createOrderItemInvalidUserThrows() {

        CreateOrderItemDTO dto = CreateOrderItemDTO.builder()
                .orderId(10L)
                .itemId(20L)
                .quantity(2L)
                .build();

        OrdersEntity order = OrdersEntity.builder()
                .id(10L)
                .userId(99L)
                .build();

        when(ordersRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderOperationException.class,
                () -> orderItemsService.create(dto));
    }

    @Test
    void createOrderItemInvalidProductThrows() {

        CreateOrderItemDTO dto = CreateOrderItemDTO.builder()
                .orderId(10L)
                .itemId(20L)
                .quantity(2L)
                .build();

        OrdersEntity order = OrdersEntity.builder()
                .id(10L)
                .userId(1L)
                .build();

        when(ordersRepository.findById(10L)).thenReturn(Optional.of(order));
        when(itemsRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> orderItemsService.create(dto));
    }

    @Test
    void deleteOrderItemSuccess() {

        OrdersEntity order = OrdersEntity.builder()
                .id(10L)
                .userId(1L)
                .build();

        OrderItemsEntity entity = OrderItemsEntity.builder()
                .id(1L)
                .order(order)
                .deleted(false)
                .build();

        when(orderItemsRepository.findById(1L)).thenReturn(Optional.of(entity));

        orderItemsService.delete(1L);

        assertTrue(Boolean.TRUE.equals(entity.getDeleted()));
        verify(orderItemsRepository).save(entity);
    }

    @Test
    void deleteOrderItemAccessDeniedThrows() {

        JwtUserDetails user = mock(JwtUserDetails.class);
        when(user.getId()).thenReturn(1L);
        when(user.getRole()).thenReturn("USER");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);

        OrdersEntity order = OrdersEntity.builder()
                .id(10L)
                .userId(99L)
                .build();

        OrderItemsEntity entity = OrderItemsEntity.builder()
                .id(1L)
                .order(order)
                .deleted(false)
                .build();

        when(orderItemsRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThrows(InvalidOrderOperationException.class,
                () -> orderItemsService.delete(1L));
    }
}