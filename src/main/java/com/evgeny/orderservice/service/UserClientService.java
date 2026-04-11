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
    public UserDTO getUserByEmail(String email) {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        Object credentials = auth.getCredentials();

        String token = null;

        if (credentials instanceof String) {
            token = (String) credentials;
        }

        return webClient.get()
                .uri("/api/v1/users/email/{email}", email)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }

    public UserDTO getUserFallback(String email, Throwable t) {
        return UserDTO.builder()
                .id(0L)
                .name("Unknown")
                .surname("Unknown")
                .email(email)
                .build();
    }

}
