package dev.marievski.fooddelivery.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddItemRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long variationId;

    @Min(1)
    private int quantity = 1;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getVariationId() { return variationId; }
    public void setVariationId(Long variationId) { this.variationId = variationId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
