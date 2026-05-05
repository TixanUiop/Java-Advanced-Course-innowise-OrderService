package com.evgeny.orderservice.kafka;

import com.evgeny.orderservice.kafka.dto.PaymentEventDTO;
import com.evgeny.orderservice.service.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderServiceImpl orderService;

    @KafkaListener(
            topics = "payment-events",
            groupId = "order-service-group"
    )
    public void handle(PaymentEventDTO event) {

        log.info("🔥 Kafka event received: {}", event);

        try {
            orderService.handlePayment(event);
        } catch (Exception e) {
            log.error("❌ Failed event ignored: {}", event, e);
        }
    }
}