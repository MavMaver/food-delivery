package dev.marievski.fooddelivery.restaurant.dto;

import java.util.List;

public class MenuItemDto {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private List<VariationDto> variations;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<VariationDto> getVariations() { return variations; }
    public void setVariations(List<VariationDto> variations) { this.variations = variations; }
}