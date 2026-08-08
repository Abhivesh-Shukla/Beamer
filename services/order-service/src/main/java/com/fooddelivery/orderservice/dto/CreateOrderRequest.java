package com.fooddelivery.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public class CreateOrderRequest {

    @NotBlank(message = "customerId is required")
    private String customerId;

    @NotBlank(message = "restaurantId is required")
    private String restaurantId;

    @NotEmpty(message = "order items list must not be empty")
    @Valid
    private List<OrderItemDto> items;

    @NotNull(message = "deliveryAddress is required")
    @Valid
    private AddressDto deliveryAddress;

    @NotNull(message = "lat is required")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double lat;

    @NotNull(message = "lng is required")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double lng;

    public CreateOrderRequest() {}

    public CreateOrderRequest(String customerId, String restaurantId, List<OrderItemDto> items,
                              AddressDto deliveryAddress, Double lat, Double lng) {
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.items = items;
        this.deliveryAddress = deliveryAddress;
        this.lat = lat;
        this.lng = lng;
    }

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
}
