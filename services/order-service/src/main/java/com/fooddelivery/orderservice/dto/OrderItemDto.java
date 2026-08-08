package com.fooddelivery.orderservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class OrderItemDto {

    @NotBlank(message = "menuItemId is required")
    private String menuItemId;

    @NotBlank(message = "item name is required")
    private String name;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0.0", message = "unitPrice must be >= 0")
    private BigDecimal unitPrice;

    public OrderItemDto() {}

    public OrderItemDto(String menuItemId, String name, Integer quantity, BigDecimal unitPrice) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getMenuItemId() { return menuItemId; }
    public void setMenuItemId(String menuItemId) { this.menuItemId = menuItemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
