package com.fooddelivery.orderservice.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.orderservice.dto.AddressDto;
import com.fooddelivery.orderservice.dto.OrderItemDto;
import com.fooddelivery.orderservice.event.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    @Test
    void sendOrderCreatedEvent_shouldSendToKafkaTopic() {
        OrderKafkaProducer producer = new OrderKafkaProducer(kafkaTemplate, objectMapper, "order-created");

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                List.of(new OrderItemDto("menu-001", "Biryani", 1, new BigDecimal("200.00"))),
                new AddressDto("123 Main St", "Chennai", "600001"),
                13.0827,
                80.2707,
                Instant.now().toString()
        );

        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")));

        // Should not throw even if Kafka is unavailable
        producer.sendOrderCreatedEvent(event);

        verify(kafkaTemplate, times(1)).send(eq("order-created"), eq(event.getOrderId()), anyString());
    }

    @Test
    void sendOrderCreatedEvent_shouldIncludeAllContractFields() throws Exception {
        OrderKafkaProducer producer = new OrderKafkaProducer(kafkaTemplate, objectMapper, "order-created");

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(anyString(), anyString(), payloadCaptor.capture()))
                .thenReturn(CompletableFuture.completedFuture(null));

        String orderId = UUID.randomUUID().toString();
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                orderId,
                "cust-001",
                "rest-001",
                List.of(new OrderItemDto("menu-001", "Biryani", 1, new BigDecimal("200.00"))),
                new AddressDto("123 Main St", "Chennai", "600001"),
                13.0827, 80.2707,
                Instant.now().toString()
        );

        producer.sendOrderCreatedEvent(event);

        String capturedJson = payloadCaptor.getValue();
        assertThat(capturedJson).contains("\"eventType\":\"order-created\"");
        assertThat(capturedJson).contains("\"orderId\":\"" + orderId + "\"");
        assertThat(capturedJson).contains("\"lat\":13.0827");
        assertThat(capturedJson).contains("\"lng\":80.2707");
        assertThat(capturedJson).contains("\"items\"");
        assertThat(capturedJson).contains("\"deliveryAddress\"");
        assertThat(capturedJson).contains("\"timestamp\"");
    }
}
