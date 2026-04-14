package com.evgeny.orderservice.it;

import com.evgeny.orderservice.dto.item.CreateItemsDTO;
import com.evgeny.orderservice.entity.Enums.AuthRole;
import com.evgeny.orderservice.entity.ItemsEntity;
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
@DisplayName("IT ItemsController")
class ItemsControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM order_items");
        jdbcTemplate.execute("DELETE FROM orders");
        jdbcTemplate.execute("DELETE FROM items");

        adminToken = jwtUtil.generateToken(1L, AuthRole.ADMIN);
        userToken = jwtUtil.generateToken(2L, AuthRole.USER);;
    }

    @Test
    void createItemSuccess() throws Exception {

        CreateItemsDTO dto = new CreateItemsDTO();
        dto.setName("Phone");
        dto.setPrice(BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/items/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Phone"));
    }

    @Test
    void createItemForbiddenForUser() throws Exception {

        CreateItemsDTO dto = new CreateItemsDTO();
        dto.setName("Phone");

        mockMvc.perform(post("/api/items/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getItemByIdSuccess() throws Exception {

        jdbcTemplate.update("""
            INSERT INTO items (id, name, price, deleted)
            VALUES (100, 'Laptop', 1500, false)
        """);

        mockMvc.perform(get("/api/items/100")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(1500));
    }

    @Test
    void getItemNotFound() throws Exception {

        mockMvc.perform(get("/api/items/999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllItemsSuccess() throws Exception {

        jdbcTemplate.execute("""
            INSERT INTO items (id, name, price, deleted)
            VALUES (1, 'A', 100, false),
                   (2, 'B', 200, false)
        """);

        mockMvc.perform(get("/api/items/all")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateItemSuccess() throws Exception {

        jdbcTemplate.execute("""
            INSERT INTO items (id, name, price, deleted)
            VALUES (1, 'Old', 100, false)
        """);

        var update = new com.evgeny.orderservice.dto.item.FullItemsDTO();
        update.setName("New");
        update.setPrice(BigDecimal.valueOf(999));

        mockMvc.perform(put("/api/items/update/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"))
                .andExpect(jsonPath("$.price").value(999));
    }

    @Test
    void updateItemForbiddenForUser() throws Exception {

        var update = new com.evgeny.orderservice.dto.item.FullItemsDTO();
        update.setName("New");
        update.setPrice(BigDecimal.TEN);

        mockMvc.perform(put("/api/items/update/1")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteItemSuccess() throws Exception {

        jdbcTemplate.execute("""
            INSERT INTO items (id, name, price, deleted)
            VALUES (1, 'Del', 100, false)
        """);

        mockMvc.perform(delete("/api/items/delete/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteItemForbiddenForUser() throws Exception {

        mockMvc.perform(delete("/api/items/delete/1")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}