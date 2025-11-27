package dev.marievski.fooddelivery.cart.dto;


import java.math.BigDecimal;


public class CartItemDto {
    private Long id;
    private Long variationId;
    private String label;
    private BigDecimal price;
    private int quantity;
    private BigDecimal lineTotal;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVariationId() { return variationId; }
    public void setVariationId(Long variationId) { this.variationId = variationId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}