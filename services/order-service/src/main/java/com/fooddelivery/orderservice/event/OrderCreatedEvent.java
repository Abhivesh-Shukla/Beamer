package com.fooddelivery.orderservice.event;

import com.fooddelivery.orderservice.dto.AddressDto;
import com.fooddelivery.orderservice.dto.OrderItemDto;
import java.util.List;

public class OrderCreatedEvent {

    private String eventId;
    private String eventType = "order-created";
    private String orderId;
    private String customerId;
    private String restaurantId;
    private List<OrderItemDto> items;
    private AddressDto deliveryAddress;
    private Double lat;
    private Double lng;
    private String timestamp;

    public OrderCreatedEvent() {}

    public OrderCreatedEvent(String eventId, String orderId, String customerId, String restaurantId,
                             List<OrderItemDto> items, AddressDto deliveryAddress,
                             Double lat, Double lng, String timestamp) {
        this.eventId = eventId;
        this.eventType = "order-created";
        this.orderId = orderId;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.items = items;
        this.deliveryAddress = deliveryAddress;
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }

    public AddressDto getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(AddressDto deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
