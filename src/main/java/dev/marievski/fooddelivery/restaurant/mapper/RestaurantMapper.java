package dev.marievski.fooddelivery.restaurant.mapper;

import dev.marievski.fooddelivery.restaurant.Restaurant;
import dev.marievski.fooddelivery.restaurant.dto.RestaurantDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {
    RestaurantDto toDto(Restaurant restaurant);
}