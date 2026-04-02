package com.evgeny.orderservice.service;

import com.evgeny.orderservice.dto.user.UserDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class UserClientService {

    private final WebClient webClient;

    @CircuitBreaker(name = "userServiceCircuitBreaker", fallbackMethod = "getUserFallback")
    public UserDTO getUserById(Long userId) {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        String token = (String) auth.getCredentials();
        return webClient.get()
                .uri("http://localhost:8080/api/v1/users/{id}", userId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }

    public UserDTO getUserFallback(Long userId, Throwable t) {
        return UserDTO.builder()
                .id(userId)
                .name("Unknown")
                .surname("Unknown")
                .email("unknown@example.com")
                .build();
    }

}
