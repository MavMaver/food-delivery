package dev.marievski.fooddelivery.cart.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


public class AddItemRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long variationId;
    @Min(1)
    private Integer quantity;


    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getVariationId() { return variationId; }
    public void setVariationId(Long variationId) { this.variationId = variationId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}