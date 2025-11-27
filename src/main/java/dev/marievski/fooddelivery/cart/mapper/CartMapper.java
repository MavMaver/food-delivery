package dev.marievski.fooddelivery.cart.mapper;

import dev.marievski.fooddelivery.cart.Cart;
import dev.marievski.fooddelivery.cart.CartItem;
import dev.marievski.fooddelivery.cart.dto.CartDto;
import dev.marievski.fooddelivery.cart.dto.CartItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "restaurantId", source = "restaurant.id")
    @Mapping(target = "items", source = "items")
    CartDto toDto(Cart cart);

    @Mapping(target = "variationId", source = "variation.id")
    @Mapping(target = "label", source = "variation.label")
    @Mapping(target = "price", source = "variation.price")
    @Mapping(target = "lineTotal", expression = "java(cartItem.getVariation().getPrice().multiply(java.math.BigDecimal.valueOf(cartItem.getQuantity())))")
    CartItemDto toDto(CartItem cartItem);

    List<CartItemDto> toDtoList(List<CartItem> cartItems);
}