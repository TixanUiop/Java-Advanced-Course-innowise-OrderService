package com.evgeny.orderservice.config;

import com.evgeny.orderservice.exception.OrderItemsNotFoundException;
import com.evgeny.orderservice.kafka.dto.PaymentEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
@Profile("!test")
public class KafkaConsumerConfig {


    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(template);

        DefaultErrorHandler handler =
                new DefaultErrorHandler(recoverer, new FixedBackOff(0L, 0L));

        handler.addNotRetryableExceptions(
                Exception.class
        );

        return handler;
    }


//    @Bean
//    public ConsumerFactory<String, PaymentEventDTO> consumerFactory() {
//
//        Map<String, Object> props = new HashMap<>();
//
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-service-group");
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//
//        JsonDeserializer<PaymentEventDTO> deserializer =
//                new JsonDeserializer<>(PaymentEventDTO.class);
//
//        deserializer.addTrustedPackages("*");
//        deserializer.setRemoveTypeHeaders(false);
//
//        return new DefaultKafkaConsumerFactory<>(
//                props,
//                new StringDeserializer(),
//                deserializer
//        );
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, PaymentEventDTO>
//    kafkaListenerContainerFactory() {
//
//        ConcurrentKafkaListenerContainerFactory<String, PaymentEventDTO> factory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//
//        factory.setConsumerFactory(consumerFactory());
//        return factory;
//    }
}