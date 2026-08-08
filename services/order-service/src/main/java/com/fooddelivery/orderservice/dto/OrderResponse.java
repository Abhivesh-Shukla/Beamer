package com.fooddelivery.orderservice.dto;

import java.time.Instant;
import java.util.List;

public class OrderResponse {

    private String id;
    private String customerId;
    private String restaurantId;
    private String status;
    private List<OrderItemDto> items;
    private AddressDto deliveryAddress;
    private Double lat;
    private Double lng;
    private Instant createdAt;

    public OrderResponse() {}

    public OrderResponse(String id, String customerId, String restaurantId, String status,
                         List<OrderItemDto> items, AddressDto deliveryAddress,
                         Double lat, Double lng, Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.status = status;
        this.items = items;
        this.deliveryAddress = deliveryAddress;
        this.lat = lat;
        this.lng = lng;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }

    public AddressDto getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(AddressDto deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
