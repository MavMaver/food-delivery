package dev.marievski.fooddelivery.cart.mapper;

import dev.marievski.fooddelivery.cart.CartItem;
import dev.marievski.fooddelivery.cart.dto.CartItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "variationId", source = "variation.id")
    @Mapping(target = "label", source = "variation.label")
    @Mapping(target = "price", source = "variation.price")
    @Mapping(target = "lineTotal", expression = "java(cartItem.getVariation().getPrice().multiply(java.math.BigDecimal.valueOf(cartItem.getQuantity())))")
    CartItemDto toDto(CartItem cartItem);
}