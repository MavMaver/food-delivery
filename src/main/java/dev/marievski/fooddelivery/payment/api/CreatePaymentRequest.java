package dev.marievski.fooddelivery.payment.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreatePaymentRequest {

    @NotNull
    private Long orderId;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true, message = "amount must be > 0")
    private BigDecimal amount;

    // идемпотентный внешний ключ (может быть null, но лучше присылать)
    private String externalId;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
}
