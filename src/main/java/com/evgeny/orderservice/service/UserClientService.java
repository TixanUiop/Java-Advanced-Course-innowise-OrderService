package com.evgeny.orderservice.service;

import com.evgeny.orderservice.dto.user.UserDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
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

        log.info("CALLING USER SERVICE: /api/v1/users/email/{}", email);

        return webClient.get()
                .uri("/api/v1/users/email/{email}", email)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .doOnNext(body -> log.error("❌ USER-SERVICE RESPONSE: {}", body))
                                .then(Mono.error(new RuntimeException("User service error")))
                )
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
