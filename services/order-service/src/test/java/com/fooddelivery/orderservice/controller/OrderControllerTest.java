package com.fooddelivery.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.orderservice.dto.AddressDto;
import com.fooddelivery.orderservice.dto.CreateOrderRequest;
import com.fooddelivery.orderservice.dto.OrderItemDto;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void postOrders_shouldReturn201WithValidRequest() throws Exception {
        // Arrange
        OrderItemDto item = new OrderItemDto("menu-001", "Chicken Biryani", 2, new BigDecimal("250.00"));
        AddressDto address = new AddressDto("123 Main Street", "Chennai", "600001");
        CreateOrderRequest request = new CreateOrderRequest("cust-001", "rest-001",
                List.of(item), address, 13.0827, 80.2707);

        OrderResponse mockResponse = new OrderResponse(
                "order-uuid-123", "cust-001", "rest-001", "CREATED",
                List.of(item), address, 13.0827, 80.2707, Instant.now()
        );
        when(orderService.createOrder(any())).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.id").value("order-uuid-123"))
                .andExpect(jsonPath("$.customerId").value("cust-001"));
    }

    @Test
    void postOrders_shouldReturn400WithMissingCustomerId() throws Exception {
        // Arrange — missing customerId
        String badRequest = """
                {
                  "restaurantId": "rest-001",
                  "items": [{"menuItemId":"m1","name":"Test","quantity":1,"unitPrice":10.0}],
                  "deliveryAddress": {"line1":"123","city":"Chennai","postalCode":"600001"},
                  "lat": 13.0,
                  "lng": 80.0
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrderById_shouldReturn200WhenFound() throws Exception {
        OrderItemDto item = new OrderItemDto("menu-001", "Chicken Biryani", 1, new BigDecimal("250.00"));
        AddressDto address = new AddressDto("123 Main Street", "Chennai", "600001");
        OrderResponse mockResponse = new OrderResponse(
                "order-uuid-123", "cust-001", "rest-001", "CREATED",
                List.of(item), address, 13.0827, 80.2707, Instant.now()
        );
        when(orderService.getOrderById("order-uuid-123")).thenReturn(mockResponse);

        mockMvc.perform(get("/orders/order-uuid-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("order-uuid-123"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getOrderById_shouldReturn404WhenNotFound() throws Exception {
        when(orderService.getOrderById("missing-id"))
                .thenThrow(new OrderService.OrderNotFoundException("Order not found with id: missing-id"));

        mockMvc.perform(get("/orders/missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}
