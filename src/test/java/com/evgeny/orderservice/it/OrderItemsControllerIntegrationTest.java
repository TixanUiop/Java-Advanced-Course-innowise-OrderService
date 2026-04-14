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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String adminToken;
    private ItemsEntity testProduct;
    private OrdersEntity testOrder;

    @BeforeEach
    void setUp() {
        ordersRepository.deleteAll();
        jdbcTemplate.execute("DELETE FROM order_items");
        jdbcTemplate.execute("DELETE FROM orders");
        jdbcTemplate.execute("DELETE FROM items");

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
                        .userEmail("test@example.com")
                        .status(OrderStatus.Collect)
                        .deleted(false)
                        .totalPrice(BigDecimal.ZERO)
                        .build()
        );
    }

    @Test
    void createOrderItemSuccess() throws Exception {
        CreateOrderItemDTO dto = CreateOrderItemDTO.builder()
                .orderId(testOrder.getId())
                .itemId(testProduct.getId())
                .quantity(3L)
                .build();

        mockMvc.perform(post("/api/order-items/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderId").value(testOrder.getId()))
                .andExpect(jsonPath("$.itemId").value(testProduct.getId()))
                .andExpect(jsonPath("$.quantity").value(3));
    }

    @Test
    void createOrderItemProductNotFound() throws Exception {
        CreateOrderItemDTO dto = CreateOrderItemDTO.builder()
                .orderId(testOrder.getId())
                .itemId(999L)
                .quantity(2L)
                .build();

        mockMvc.perform(post("/api/order-items/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrderItemZeroQuantity() throws Exception {
        CreateOrderItemDTO dto = CreateOrderItemDTO.builder()
                .orderId(testOrder.getId())
                .itemId(testProduct.getId())
                .quantity(0L)
                .build();

        mockMvc.perform(post("/api/order-items/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantity").value("must be greater than or equal to 1"));
    }

    @Test
    void getOrderItemByIdSuccess() throws Exception {
        OrderItemsEntity entity = orderItemsRepository.save(
                OrderItemsEntity.builder()
                        .order(testOrder)
                        .item(testProduct)
                        .quantity(2L)
                        .deleted(false)
                        .build()
        );

        mockMvc.perform(get("/api/order-items/" + entity.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entity.getId()))
                .andExpect(jsonPath("$.orderId").value(testOrder.getId()))
                .andExpect(jsonPath("$.itemId").value(testProduct.getId()))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void getOrderItemByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/order-items/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}