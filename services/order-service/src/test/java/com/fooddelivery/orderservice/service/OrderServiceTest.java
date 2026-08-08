package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.dto.AddressDto;
import com.fooddelivery.orderservice.dto.CreateOrderRequest;
import com.fooddelivery.orderservice.dto.OrderItemDto;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.entity.OrderEntity;
import com.fooddelivery.orderservice.entity.OrderItemEntity;
import com.fooddelivery.orderservice.producer.OrderKafkaProducer;
import com.fooddelivery.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderKafkaProducer orderKafkaProducer;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, orderKafkaProducer);
    }

    @Test
    void createOrder_shouldPersistOrderAndPublishEvent() {
        // Arrange
        CreateOrderRequest request = buildCreateOrderRequest();

        OrderEntity savedEntity = buildOrderEntity();
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedEntity);
        doNothing().when(orderKafkaProducer).sendOrderCreatedEvent(any());

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("CREATED");
        assertThat(response.getCustomerId()).isEqualTo("cust-001");
        assertThat(response.getRestaurantId()).isEqualTo("rest-001");
        assertThat(response.getLat()).isEqualTo(13.0827);
        assertThat(response.getLng()).isEqualTo(80.2707);

        verify(orderRepository, times(1)).save(any(OrderEntity.class));
        verify(orderKafkaProducer, times(1)).sendOrderCreatedEvent(any());
    }

    @Test
    void getOrderById_shouldReturnOrderIfFound() {
        // Arrange
        OrderEntity entity = buildOrderEntity();
        when(orderRepository.findById("order-uuid-123")).thenReturn(Optional.of(entity));

        // Act
        OrderResponse response = orderService.getOrderById("order-uuid-123");

        // Assert
        assertThat(response.getId()).isEqualTo("order-uuid-123");
        assertThat(response.getStatus()).isEqualTo("CREATED");
    }

    @Test
    void getOrderById_shouldThrowIfOrderNotFound() {
        when(orderRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById("nonexistent"))
                .isInstanceOf(OrderService.OrderNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    private CreateOrderRequest buildCreateOrderRequest() {
        OrderItemDto item = new OrderItemDto("menu-001", "Chicken Biryani", 2, new BigDecimal("250.00"));
        AddressDto address = new AddressDto("123 Main Street", "Chennai", "600001");
        return new CreateOrderRequest("cust-001", "rest-001", List.of(item), address, 13.0827, 80.2707);
    }

    private OrderEntity buildOrderEntity() {
        Instant now = Instant.now();
        OrderEntity entity = new OrderEntity(
                "order-uuid-123", "cust-001", "rest-001", "CREATED",
                "123 Main Street", "Chennai", "600001",
                13.0827, 80.2707, now, now
        );
        entity.addItem(new OrderItemEntity("menu-001", "Chicken Biryani", 2, new BigDecimal("250.00")));
        return entity;
    }
}
