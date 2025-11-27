package dev.marievski.fooddelivery.order.mapper;

import dev.marievski.fooddelivery.order.Order;
import dev.marievski.fooddelivery.order.OrderItem;
import dev.marievski.fooddelivery.order.dto.OrderDto;
import dev.marievski.fooddelivery.order.dto.OrderItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "status", expression = "java(order.getStatus() != null ? order.getStatus().name() : null)")
    @Mapping(target = "items", source = "items")
    OrderDto toDto(Order order);

    @Mapping(target = "lineTotal", expression = "java(orderItem.getPrice().multiply(java.math.BigDecimal.valueOf(orderItem.getQuantity())))")
    OrderItemDto toDto(OrderItem orderItem);

    List<OrderDto> toDtoList(List<Order> orders);
    List<OrderItemDto> toItemDtoList(List<OrderItem> orderItems);
}