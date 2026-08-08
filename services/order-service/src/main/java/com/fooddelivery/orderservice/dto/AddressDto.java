package com.fooddelivery.orderservice.dto;

import jakarta.validation.constraints.NotBlank;

public class AddressDto {

    @NotBlank(message = "line1 must not be blank")
    private String line1;

    @NotBlank(message = "city must not be blank")
    private String city;

    @NotBlank(message = "postalCode must not be blank")
    private String postalCode;

    public AddressDto() {}

    public AddressDto(String line1, String city, String postalCode) {
        this.line1 = line1;
        this.city = city;
        this.postalCode = postalCode;
    }

    public String getLine1() { return line1; }
    public void setLine1(String line1) { this.line1 = line1; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
}
