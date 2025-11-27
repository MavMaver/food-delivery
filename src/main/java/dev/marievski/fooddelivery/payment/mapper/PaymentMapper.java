package dev.marievski.fooddelivery.payment.mapper;

import dev.marievski.fooddelivery.payment.Payment;
import dev.marievski.fooddelivery.payment.dto.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "orderId", source = "order.id")
    PaymentResponse toDto(Payment payment);

    List<PaymentResponse> toDtoList(List<Payment> payments);
}