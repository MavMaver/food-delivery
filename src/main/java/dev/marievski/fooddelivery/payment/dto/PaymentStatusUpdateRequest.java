package dev.marievski.fooddelivery.payment.dto;


import dev.marievski.fooddelivery.payment.PaymentStatus;
import jakarta.validation.constraints.NotNull;


public class PaymentStatusUpdateRequest {
    @NotNull
    private PaymentStatus status;


    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
}