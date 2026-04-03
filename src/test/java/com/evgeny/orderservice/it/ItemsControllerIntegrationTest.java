package com.evgeny.orderservice.it;

import com.evgeny.orderservice.dto.item.CreateItemsDTO;
import com.evgeny.orderservice.dto.item.FullItemsDTO;
import com.evgeny.orderservice.entity.ItemsEntity;
import com.evgeny.orderservice.entity.Enums.AuthRole;
import com.evgeny.orderservice.repository.ItemsRepository;
import com.evgeny.orderservice.repository.OrderItemsRepository;
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
@DisplayName("IT ItemsController")
class ItemsControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemsRepository itemsRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;


    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        orderItemsRepository.deleteAll();
        itemsRepository.deleteAll();

        adminToken = jwtUtil.generateToken(1L, AuthRole.ADMIN);
        userToken = jwtUtil.generateToken(2L, AuthRole.USER);
    }

    @Test
    @DisplayName("GET /api/items/all - should return empty list initially")
    void getAllItemsEmptyList() throws Exception {
        mockMvc.perform(get("/api/items/all"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("POST /api/items/create - should create item for ADMIN")
    void createItemAdminSuccess() throws Exception {
        CreateItemsDTO dto = CreateItemsDTO.builder()
                .name("Test Product")
                .price(BigDecimal.valueOf(10.5))
                .build();

        mockMvc.perform(post("/api/items/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(10.5));

        assertThat(itemsRepository.existsByName("Test Product")).isTrue();
    }

    @Test
    @DisplayName("POST /api/items/create - should fail for USER role")
    void createItemUserForbidden() throws Exception {
        CreateItemsDTO dto = CreateItemsDTO.builder()
                .name("Test Product 2")
                .price(BigDecimal.valueOf(20))
                .build();

        mockMvc.perform(post("/api/items/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/items/{id} - fetch created item")
    void getByIdSuccess() throws Exception {

        ItemsEntity item = itemsRepository.save(
                ItemsEntity.builder()
                        .name("Product A")
                        .price(BigDecimal.valueOf(2))
                        .build()
        );

        mockMvc.perform(get("/api/items/" + item.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath("$.name").value("Product A"));
    }

    @Test
    @DisplayName("PUT /api/items/update/{id} - update item as ADMIN")
    void updateItemAdminSuccess() throws Exception {
        ItemsEntity item = itemsRepository.save(
                ItemsEntity.builder().name("Old Name").price(BigDecimal.valueOf(10)).build()
        );

        FullItemsDTO updateDto = FullItemsDTO.builder()
                .name("New Name")
                .price(BigDecimal.valueOf(12))
                .build();

        mockMvc.perform(put("/api/items/update/" + item.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.price").value(12));
    }

    @Test
    @DisplayName("DELETE /api/items/delete/{id} - delete item as ADMIN")
    void deleteItemAdminSuccess() throws Exception {
        ItemsEntity item = itemsRepository.save(
                ItemsEntity.builder().name("ToDelete").price(BigDecimal.valueOf(5)).build()
        );

        mockMvc.perform(delete("/api/items/delete/" + item.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertThat(itemsRepository.existsById(item.getId())).isFalse();
    }

}