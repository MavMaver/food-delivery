package dev.marievski.fooddelivery.restaurant.dto;

import dev.marievski.fooddelivery.common.Cuisine;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RestaurantRequest {
    @NotBlank
    private String name;
    @NotNull
    private Cuisine cuisine;
    private Boolean open;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Cuisine getCuisine() { return cuisine; }
    public void setCuisine(Cuisine cuisine) { this.cuisine = cuisine; }
    public Boolean getOpen() { return open; }
    public void setOpen(Boolean open) { this.open = open; }
}