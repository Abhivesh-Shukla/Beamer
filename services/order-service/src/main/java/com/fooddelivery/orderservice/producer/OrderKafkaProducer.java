package com.fooddelivery.orderservice.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.orderservice.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderKafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(OrderKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topicName;

    public OrderKafkaProducer(KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper,
                               @Value("${kafka.topics.order-created:order-created}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topicName = topicName;
    }

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topicName, event.getOrderId(), jsonPayload)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            logger.info("Published order-created event for orderId={}, offset={}",
                                    event.getOrderId(), result.getRecordMetadata().offset());
                        } else {
                            logger.error("Failed to publish order-created event for orderId={}",
                                    event.getOrderId(), ex);
                        }
                    });
        } catch (Exception e) {
            logger.error("Error serializing order-created event for orderId={}", event.getOrderId(), e);
        }
    }
}
