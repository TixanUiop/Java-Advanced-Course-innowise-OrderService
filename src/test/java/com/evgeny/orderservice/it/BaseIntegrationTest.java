package com.evgeny.orderservice.it;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest
public abstract class BaseIntegrationTest {

    protected static WireMockServer wireMockServer;

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password")
            .withReuse(true);

    @BeforeAll
    static void init() {

        if (!postgres.isRunning()) {
            postgres.start();
            System.out.println("🟢 PostgreSQL is starting on : " + postgres.getJdbcUrl());
        }

        if (wireMockServer == null || !wireMockServer.isRunning()) {
            wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
            wireMockServer.start();

            setupWireMockStubs();

            System.out.println("🟢 WireMock has started on: " + wireMockServer.baseUrl());
        }

        System.setProperty("user.service.url", wireMockServer.baseUrl());
    }

    private static void setupWireMockStubs() {
        wireMockServer.stubFor(
                get(urlMatching("/api/v1/users/.*"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody("""
                                    {
                                        "id": 1,
                                        "email": "mocked@example.com",
                                        "name": "Mocked User"
                                    }
                                    """)));
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> postgres.getUsername());
        registry.add("spring.datasource.password", () -> postgres.getPassword());
        registry.add("user.service.url", () -> wireMockServer.baseUrl());

        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "5");
        registry.add("spring.datasource.hikari.max-lifetime", () -> "30000");
        registry.add("spring.datasource.hikari.connection-timeout", () -> "5000");
        registry.add("spring.datasource.hikari.validation-timeout", () -> "3000");
    }

    @AfterAll
    static void cleanup() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }
}