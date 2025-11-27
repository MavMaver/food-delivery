package dev.marievski.fooddelivery.restaurant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class MenuItemRequest {
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private List<@Valid VariationRequest> variations;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<VariationRequest> getVariations() { return variations; }
    public void setVariations(List<VariationRequest> variations) { this.variations = variations; }
}