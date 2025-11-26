package dev.marievski.fooddelivery.payment.api;

import dev.marievski.fooddelivery.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class PaymentResponse {

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String externalId;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() { return id; }
    public PaymentResponse setId(Long id) { this.id = id; return this; }

    public Long getOrderId() { return orderId; }
    public PaymentResponse setOrderId(Long orderId) { this.orderId = orderId; return this; }

    public BigDecimal getAmount() { return amount; }
    public PaymentResponse setAmount(BigDecimal amount) { this.amount = amount; return this; }

    public PaymentStatus getStatus() { return status; }
    public PaymentResponse setStatus(PaymentStatus status) { this.status = status; return this; }

    public String getExternalId() { return externalId; }
    public PaymentResponse setExternalId(String externalId) { this.externalId = externalId; return this; }

    public Instant getCreatedAt() { return createdAt; }
    public PaymentResponse setCreatedAt(Instant createdAt) { this.createdAt = createdAt; return this; }

    public Instant getUpdatedAt() { return updatedAt; }
    public PaymentResponse setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
}
