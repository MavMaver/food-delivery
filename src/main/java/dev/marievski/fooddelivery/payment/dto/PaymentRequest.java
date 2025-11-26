package dev.marievski.fooddelivery.payment;

import java.math.BigDecimal;

/** DTO для POST /payments */
public class PaymentRequest {
    private Long orderId;
    private BigDecimal amount;
    private String externalId;

    public PaymentRequest() {}

    public PaymentRequest(Long orderId, BigDecimal amount, String externalId) {
        this.orderId = orderId;
        this.amount = amount;
        this.externalId = externalId;
    }

    public Long getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public String getExternalId() { return externalId; }

    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
}
