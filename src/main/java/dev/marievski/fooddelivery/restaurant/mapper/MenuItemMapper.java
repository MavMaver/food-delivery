package dev.marievski.fooddelivery.restaurant.mapper;

import dev.marievski.fooddelivery.restaurant.MenuItem;
import dev.marievski.fooddelivery.restaurant.MenuVariation;
import dev.marievski.fooddelivery.restaurant.dto.MenuItemDto;
import dev.marievski.fooddelivery.restaurant.dto.VariationDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {

    MenuItemDto toDto(MenuItem menuItem);

    List<MenuItemDto> toDtoList(List<MenuItem> menuItems);

    VariationDto toVariationDto(MenuVariation menuVariation);

    List<VariationDto> toVariationDtoList(List<MenuVariation> menuVariations);
}