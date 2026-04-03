package com.evgeny.orderservice.it;

import com.evgeny.orderservice.dto.orderItems.CreateOrderItemDTO;
import com.evgeny.orderservice.entity.Enums.AuthRole;
import com.evgeny.orderservice.entity.Enums.OrderStatus;
import com.evgeny.orderservice.entity.ItemsEntity;
import com.evgeny.orderservice.entity.OrderItemsEntity;
import com.evgeny.orderservice.entity.OrdersEntity;
import com.evgeny.orderservice.repository.ItemsRepository;
import com.evgeny.orderservice.repository.OrderItemsRepository;
import com.evgeny.orderservice.repository.OrdersRepository;
import com.evgeny.orderservice.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("IT OrderItemsController")
class OrderItemsControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ItemsRepository itemsRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;
    private ItemsEntity testProduct;
    private OrdersEntity testOrder;

    @BeforeEach
    void setUp() {
        orderItemsRepository.deleteAll();
        ordersRepository.deleteAll();
        itemsRepository.deleteAll();

        adminToken = jwtUtil.generateToken(1L, AuthRole.ADMIN);

        testProduct = itemsRepository.save(
                ItemsEntity.builder()
                        .name("Test Product")
                        .price(BigDecimal.valueOf(100))
                        .build()
        );

        testOrder = ordersRepository.save(
                OrdersEntity.builder()
                        .userId(1L)
                        .status(OrderStatus.Collect)
                        .deleted(false)
                        .totalPrice(BigDecimal.ZERO)
                        .build()
        );
    }

    @Test
    @DisplayName("POST /api/order-items/create - should create order item")
    void createOrderItemSuccess() throws Exception {
        CreateOrderItemDTO createDTO = CreateOrderItemDTO.builder()
                .orderId(testOrder.getId())
                .itemId(testProduct.getId())
                .quantity(3L)
                .build();

        mockMvc.perform(post("/api/order-items/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderId").value(testOrder.getId()))
                .andExpect(jsonPath("$.itemId").value(testProduct.getId()))
                .andExpect(jsonPath("$.quantity").value(3));

        assertThat(orderItemsRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /api/order-items/create - should return 404 when product not found")
    void createOrderItemProductNotFound() throws Exception {
        CreateOrderItemDTO createDTO = CreateOrderItemDTO.builder()
                .orderId(testOrder.getId())
                .itemId(999L)
                .quantity(2L)
                .build();

        mockMvc.perform(post("/api/order-items/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/order-items/create - should return 400 when quantity is zero")
    void createOrderItemZeroQuantity() throws Exception {
        CreateOrderItemDTO createDTO = CreateOrderItemDTO.builder()
                .orderId(testOrder.getId())
                .itemId(testProduct.getId())
                .quantity(0L)
                .build();

        mockMvc.perform(post("/api/order-items/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Quantity must be greater than zero"));
    }

    @Test
    @DisplayName("GET /api/order-items/{id} - should get order item by id")
    void getOrderItemByIdSuccess() throws Exception {
        OrderItemsEntity orderItem = createTestOrderItem();

        mockMvc.perform(get("/api/order-items/" + orderItem.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderItem.getId()))
                .andExpect(jsonPath("$.orderId").value(testOrder.getId()))
                .andExpect(jsonPath("$.itemId").value(testProduct.getId()))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    @DisplayName("GET /api/order-items/{id} - should return 404 when order item not found")
    void getOrderItemByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/order-items/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }


    private OrderItemsEntity createTestOrderItem() {
        OrderItemsEntity orderItem = OrderItemsEntity.builder()
                .order(testOrder)
                .item(testProduct)
                .quantity(2L)
                .deleted(false)
                .build();

        return orderItemsRepository.save(orderItem);
    }
}