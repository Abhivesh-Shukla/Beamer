package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.dto.AddressDto;
import com.fooddelivery.orderservice.dto.CreateOrderRequest;
import com.fooddelivery.orderservice.dto.OrderItemDto;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.entity.OrderEntity;
import com.fooddelivery.orderservice.entity.OrderItemEntity;
import com.fooddelivery.orderservice.event.OrderCreatedEvent;
import com.fooddelivery.orderservice.producer.OrderKafkaProducer;
import com.fooddelivery.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderKafkaProducer orderKafkaProducer;

    public OrderService(OrderRepository orderRepository, OrderKafkaProducer orderKafkaProducer) {
        this.orderRepository = orderRepository;
        this.orderKafkaProducer = orderKafkaProducer;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        OrderEntity entity = new OrderEntity(
                orderId,
                request.getCustomerId(),
                request.getRestaurantId(),
                "CREATED",
                request.getDeliveryAddress().getLine1(),
                request.getDeliveryAddress().getCity(),
                request.getDeliveryAddress().getPostalCode(),
                request.getLat(),
                request.getLng(),
                now,
                now
        );

        for (OrderItemDto itemDto : request.getItems()) {
            OrderItemEntity itemEntity = new OrderItemEntity(
                    itemDto.getMenuItemId(),
                    itemDto.getName(),
                    itemDto.getQuantity(),
                    itemDto.getUnitPrice()
            );
            entity.addItem(itemEntity);
        }

        OrderEntity savedEntity = orderRepository.save(entity);
        logger.info("Order persisted successfully with orderId={}, status=CREATED", orderId);

        // Construct & publish Kafka event
        String eventId = UUID.randomUUID().toString();
        String timestampIso = DateTimeFormatter.ISO_INSTANT.format(now);
        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,
                orderId,
                savedEntity.getCustomerId(),
                savedEntity.getRestaurantId(),
                request.getItems(),
                request.getDeliveryAddress(),
                savedEntity.getLat(),
                savedEntity.getLng(),
                timestampIso
        );

        orderKafkaProducer.sendOrderCreatedEvent(event);

        return mapToResponse(savedEntity);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String id) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return mapToResponse(entity);
    }

    private OrderResponse mapToResponse(OrderEntity entity) {
        AddressDto address = new AddressDto(
                entity.getDeliveryLine1(),
                entity.getDeliveryCity(),
                entity.getDeliveryPostalCode()
        );

        List<OrderItemDto> itemDtos = entity.getItems().stream()
                .map(item -> new OrderItemDto(
                        item.getMenuItemId(),
                        item.getName(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                entity.getId(),
                entity.getCustomerId(),
                entity.getRestaurantId(),
                entity.getStatus(),
                itemDtos,
                address,
                entity.getLat(),
                entity.getLng(),
                entity.getCreatedAt()
        );
    }

    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String message) { super(message); }
    }
}
