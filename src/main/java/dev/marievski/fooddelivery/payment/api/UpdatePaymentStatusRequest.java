package dev.marievski.fooddelivery.payment.api;

import dev.marievski.fooddelivery.payment.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdatePaymentStatusRequest {

    @NotNull
    private PaymentStatus status;

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
}
