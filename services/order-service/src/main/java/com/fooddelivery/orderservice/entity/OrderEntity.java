package com.fooddelivery.orderservice.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "customer_id", length = 36, nullable = false)
    private String customerId;

    @Column(name = "restaurant_id", length = 36, nullable = false)
    private String restaurantId;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "delivery_line1", nullable = false)
    private String deliveryLine1;

    @Column(name = "delivery_city", length = 100, nullable = false)
    private String deliveryCity;

    @Column(name = "delivery_postal_code", length = 20, nullable = false)
    private String deliveryPostalCode;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lng", nullable = false)
    private Double lng;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItemEntity> items = new ArrayList<>();

    public OrderEntity() {}

    public OrderEntity(String id, String customerId, String restaurantId, String status,
                       String deliveryLine1, String deliveryCity, String deliveryPostalCode,
                       Double lat, Double lng, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.status = status;
        this.deliveryLine1 = deliveryLine1;
        this.deliveryCity = deliveryCity;
        this.deliveryPostalCode = deliveryPostalCode;
        this.lat = lat;
        this.lng = lng;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDeliveryLine1() { return deliveryLine1; }
    public void setDeliveryLine1(String deliveryLine1) { this.deliveryLine1 = deliveryLine1; }

    public String getDeliveryCity() { return deliveryCity; }
    public void setDeliveryCity(String deliveryCity) { this.deliveryCity = deliveryCity; }

    public String getDeliveryPostalCode() { return deliveryPostalCode; }
    public void setDeliveryPostalCode(String deliveryPostalCode) { this.deliveryPostalCode = deliveryPostalCode; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<OrderItemEntity> getItems() { return items; }
    public void setItems(List<OrderItemEntity> items) { this.items = items; }
}
