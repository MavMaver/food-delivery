package dev.marievski.fooddelivery.payment;

import dev.marievski.fooddelivery.payment.api.PaymentResponse;

final class PaymentMapper {
    private PaymentMapper() {}

    static PaymentResponse toResponse(Payment p) {
        return new PaymentResponse()
                .setId(p.getId())
                .setOrderId(p.getOrder().getId())
                .setAmount(p.getAmount())
                .setStatus(p.getStatus())
                .setExternalId(p.getExternalId())
                .setCreatedAt(p.getCreatedAt())
                .setUpdatedAt(p.getUpdatedAt());
    }
}
