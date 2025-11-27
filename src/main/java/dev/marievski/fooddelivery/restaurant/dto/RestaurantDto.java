package dev.marievski.fooddelivery.restaurant.dto;

import dev.marievski.fooddelivery.common.Cuisine;

public class RestaurantDto {
    private Long id;
    private String name;
    private Cuisine cuisine;
    private boolean open;
    private double rating;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Cuisine getCuisine() { return cuisine; }
    public void setCuisine(Cuisine cuisine) { this.cuisine = cuisine; }
    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}