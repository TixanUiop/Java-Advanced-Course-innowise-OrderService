package com.evgeny.orderservice.it;

import com.evgeny.orderservice.dto.order.CreateOrderDTO;
import com.evgeny.orderservice.dto.order.CreateOrderItemDTO;
import com.evgeny.orderservice.dto.order.FullOrderDTO;
import com.evgeny.orderservice.dto.order.OrderItemsDTO;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;


import java.math.BigDecimal;
import java.util.List;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
@AutoConfigureMockMvc
@DisplayName("IT OrdersController")
class OrdersControllerIntegrationTest extends BaseIntegrationTest {

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
    private String userToken;
    private ItemsEntity testProduct;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @BeforeEach
    void setUp() {
        ordersRepository.deleteAll();
        jdbcTemplate.execute("DELETE FROM order_items");
        jdbcTemplate.execute("DELETE FROM orders");
        jdbcTemplate.execute("DELETE FROM items");

        adminToken = jwtUtil.generateToken(1L, AuthRole.ADMIN);
        userToken = jwtUtil.generateToken(2L, AuthRole.USER);

        testProduct = itemsRepository.save(
                ItemsEntity.builder()
                        .name("Test Product")
                        .price(BigDecimal.valueOf(100))
                        .build()
        );
    }

    @BeforeEach
    void cleanWireMock() {
        wireMockServer.resetAll();
    }

    private void mockUserService(String email) {
        wireMockServer.stubFor(
                com.github.tomakehurst.wiremock.client.WireMock.get("/api/v1/users/email/" + email)
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                    {
                      "id": 1,
                      "email": "%s",
                      "name": "Test User"
                    }
                """.formatted(email))
                        )
        );
    }


    @Test
    @DisplayName("GET /api/orders - should return paginated filtered orders with user enrichment")
    void getOrdersFilteredWithPagination() throws Exception {

        String email = "user@example.com";
        mockUserService(email);

        createTestOrder(2L, email, OrderStatus.Collect);
        createTestOrder(2L, email, OrderStatus.Accepted);
        createTestOrder(1L, "other@example.com", OrderStatus.Collect);

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("statuses", "Collect")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].status").value("Collect"));
    }

    @Test
    @DisplayName("GET /api/orders - should filter by status and pagination")
    void getOrdersFilterByStatus() throws Exception {

        String email = "user@example.com";
        mockUserService(email);

        createTestOrder(2L, email, OrderStatus.Collect);
        createTestOrder(2L, email, OrderStatus.Accepted);

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("statuses", "Accepted")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("Accepted"));
    }



    @Test
    @DisplayName("POST /api/orders/create - should create order successfully")
    void createOrderSuccess() throws Exception {
        CreateOrderItemDTO itemDto = CreateOrderItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(2L)
                .build();

        CreateOrderDTO createOrderDTO = CreateOrderDTO.builder()
                .userId(2L)
                .userEmail("user@example.com")
                .status(OrderStatus.Collect)
                .orderItems(List.of(itemDto))
                .build();

        mockMvc.perform(post("/api/orders/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(2L))
                .andExpect(jsonPath("$.totalPrice").value(200))
                .andExpect(jsonPath("$.status").value("Collect"))
                .andExpect(jsonPath("$.orderItems[0].productId").value(testProduct.getId()))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(2));

        assertThat(ordersRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /api/orders/create - should fail when order items are empty")
    void createOrderEmptyItems() throws Exception {
        CreateOrderDTO createOrderDTO = CreateOrderDTO.builder()
                .userId(2L)
                .userEmail("user@example.com")
                .status(OrderStatus.Collect)
                .orderItems(List.of())
                .build();

        mockMvc.perform(post("/api/orders/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/orders/create - should fail when product not found")
    void createOrderProductNotFound() throws Exception {
        CreateOrderItemDTO itemDto = CreateOrderItemDTO.builder()
                .productId(999L)
                .quantity(1L)
                .build();

        CreateOrderDTO createOrderDTO = CreateOrderDTO.builder()
                .userId(2L)
                .userEmail("user@example.com")
                .status(OrderStatus.Collect)
                .orderItems(List.of(itemDto))
                .build();

        mockMvc.perform(post("/api/orders/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/orders/create - should fail when quantity is zero or negative")
    void createOrderInvalidQuantity() throws Exception {
        CreateOrderItemDTO itemDto = CreateOrderItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(0L)
                .build();

        CreateOrderDTO createOrderDTO = CreateOrderDTO.builder()
                .userId(2L)
                .userEmail("user@example.com")
                .status(OrderStatus.Collect)
                .orderItems(List.of(itemDto))
                .build();

        mockMvc.perform(post("/api/orders/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/orders/{id} - should return order by id for admin")
    void getOrderByIdAsAdmin() throws Exception {
        OrdersEntity order = createTestOrder(1L, "admin@example.com", OrderStatus.Collect);

        mockMvc.perform(get("/api/orders/" + order.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.totalPrice").value(200))
                .andExpect(jsonPath("$.status").value("Collect"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - should return 404 when order not found")
    void getOrderByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/orders/user/{userId} - should return orders by user id")
    void getOrdersByUserId() throws Exception {
        createTestOrder(2L, "user2@example.com", OrderStatus.Collect);
        createTestOrder(2L, "user2@example.com", OrderStatus.Accepted);
        createTestOrder(1L, "user1@example.com", OrderStatus.Collect);

        mockMvc.perform(get("/api/orders/user/2")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].user.email").value("user2@example.com"));

    }

    @Test
    @DisplayName("PUT /api/orders/update/{id} - should update order successfully as admin")
    void updateOrderSuccess() throws Exception {
        OrdersEntity order = createTestOrder(1L, "admin@example.com", OrderStatus.Collect);

        OrderItemsDTO updatedItem = OrderItemsDTO.builder()
                .id(order.getOrderItems().get(0).getId())
                .productId(testProduct.getId())
                .quantity(5L)
                .build();

        FullOrderDTO updateDTO = FullOrderDTO.builder()
                .id(order.getId())
                .userId(1L)
                .status(OrderStatus.Accepted)
                .deleted(false)
                .orderItems(List.of(updatedItem))
                .build();

        mockMvc.perform(put("/api/orders/update/" + order.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Accepted"))
                .andExpect(jsonPath("$.totalPrice").value(500));
    }

    @Test
    @DisplayName("PUT /api/orders/update/{id} - should fail when order items empty")
    void updateOrderEmptyItems() throws Exception {
        OrdersEntity order = createTestOrder(1L, "admin@example.com", OrderStatus.Collect);

        FullOrderDTO updateDTO = FullOrderDTO.builder()
                .id(order.getId())
                .userId(1L)
                .status(OrderStatus.Accepted)
                .deleted(false)
                .orderItems(List.of())
                .build();

        mockMvc.perform(put("/api/orders/update/" + order.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/orders/delete/{id} - should soft delete order as admin")
    void softDeleteOrderSuccess() throws Exception {
        OrdersEntity order = createTestOrder(1L, "admin@example.com", OrderStatus.Collect);

        mockMvc.perform(delete("/api/orders/delete/" + order.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        Boolean deleted = jdbcTemplate.queryForObject(
                "SELECT deleted FROM orders WHERE id = ?",
                Boolean.class,
                order.getId()
        );
        assertThat(deleted).isTrue();
    }


    @Test
    @DisplayName("DELETE /api/orders/delete/{id} - should return 404 when order not found")
    void softDeleteOrderNotFound() throws Exception {
        mockMvc.perform(delete("/api/orders/delete/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/orders/create - should create order with multiple items")
    void createOrderWithMultipleItems() throws Exception {
        ItemsEntity secondProduct = itemsRepository.save(
                ItemsEntity.builder()
                        .name("Second Product")
                        .price(BigDecimal.valueOf(50))
                        .build()
        );

        CreateOrderItemDTO item1 = CreateOrderItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(2L)
                .build();

        CreateOrderItemDTO item2 = CreateOrderItemDTO.builder()
                .productId(secondProduct.getId())
                .quantity(3L)
                .build();

        CreateOrderDTO createOrderDTO = CreateOrderDTO.builder()
                .userId(2L)
                .userEmail("user@example.com")
                .status(OrderStatus.Collect)
                .orderItems(List.of(item1, item2))
                .build();

        mockMvc.perform(post("/api/orders/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value(350))
                .andExpect(jsonPath("$.orderItems.length()").value(2));
    }


    private OrdersEntity createTestOrder(Long userId, String userEmail, OrderStatus status) {
        BigDecimal totalPrice = testProduct.getPrice().multiply(BigDecimal.valueOf(2L));

        OrdersEntity order = OrdersEntity.builder()
                .userId(userId)
                .userEmail(userEmail)
                .status(status)
                .deleted(false)
                .totalPrice(totalPrice)
                .build();

        OrdersEntity savedOrder = ordersRepository.save(order);

        OrderItemsEntity orderItem = OrderItemsEntity.builder()
                .item(testProduct)
                .quantity(2L)
                .order(savedOrder)
                .deleted(false)
                .build();

        OrderItemsEntity savedOrderItem = orderItemsRepository.save(orderItem);

        savedOrder.setOrderItems(List.of(savedOrderItem));

        return ordersRepository.save(savedOrder);
    }
}