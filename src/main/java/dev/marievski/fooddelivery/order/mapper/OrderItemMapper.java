package dev.marievski.fooddelivery.order.mapper;

import dev.marievski.fooddelivery.order.OrderItem;
import dev.marievski.fooddelivery.order.dto.OrderItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "lineTotal", expression = "java(orderItem.getPrice().multiply(java.math.BigDecimal.valueOf(orderItem.getQuantity())))")
    OrderItemDto toDto(OrderItem orderItem);
}