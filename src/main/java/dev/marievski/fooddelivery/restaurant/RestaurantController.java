package dev.marievski.fooddelivery.restaurant;

import dev.marievski.fooddelivery.common.Cuisine;
import dev.marievski.fooddelivery.restaurant.dto.*;
import dev.marievski.fooddelivery.restaurant.mapper.MenuItemMapper;
import dev.marievski.fooddelivery.restaurant.mapper.RestaurantMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RestaurantController {

    private final RestaurantService service;
    private final RestaurantMapper restaurantMapper;
    private final MenuItemMapper menuItemMapper;

    public RestaurantController(RestaurantService service, RestaurantMapper restaurantMapper, MenuItemMapper menuItemMapper) {
        this.service = service;
        this.restaurantMapper = restaurantMapper;
        this.menuItemMapper = menuItemMapper;
    }

    @PostMapping("/restaurants")
    public RestaurantDto createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setOpen(request.getOpen() == null || request.getOpen());
        return restaurantMapper.toDto(service.create(restaurant));
    }

    @GetMapping("/restaurants")
    public Page<RestaurantDto> listRestaurants(@RequestParam(required = false) Cuisine cuisine,
                                               @RequestParam(required = false) Double minRating,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return service.list(cuisine, minRating, PageRequest.of(page, size))
                .map(restaurantMapper::toDto);
    }

    @GetMapping("/restaurants/{id}")
    public RestaurantDto getRestaurant(@PathVariable Long id) {
        return restaurantMapper.toDto(service.getOrThrow(id));
    }

    @PutMapping("/restaurants/{id}")
    public RestaurantDto updateRestaurant(@PathVariable Long id, @Valid @RequestBody RestaurantRequest request) {
        Restaurant patch = new Restaurant();
        patch.setName(request.getName());
        patch.setCuisine(request.getCuisine());
        patch.setOpen(request.getOpen() == null || request.getOpen());
        return restaurantMapper.toDto(service.update(id, patch));
    }

    @DeleteMapping("/restaurants/{id}")
    public void deleteRestaurant(@PathVariable Long id) {
        service.close(id);
    }

    @PostMapping("/restaurants/{id}/menu")
    public MenuItemDto addDish(@PathVariable Long id, @Valid @RequestBody MenuItemRequest request) {
        List<MenuVariation> variations = request.getVariations().stream().map(v -> {
            MenuVariation mv = new MenuVariation();
            mv.setLabel(v.getLabel());
            mv.setPrice(v.getPrice());
            mv.setCookingMinutes(v.getCookingMinutes());
            mv.setAvailable(v.getAvailable() == null || v.getAvailable());
            return mv;
        }).toList();

        MenuItem saved = service.addDish(id, request.getName(), request.getDescription(), variations);
        return menuItemMapper.toDto(saved);
    }

    @GetMapping("/restaurants/{id}/menu")
    public List<MenuItemDto> menu(@PathVariable Long id) {
        return menuItemMapper.toDtoList(service.menuOf(id));
    }

    @PutMapping("/menu/{id}")
    public MenuItemDto updateDish(@PathVariable Long id, @Valid @RequestBody MenuItemUpdateRequest request) {
        MenuItem item = service.updateDish(id, request.getName(), request.getDescription(), request.getActive());
        return menuItemMapper.toDto(item);
    }

    @DeleteMapping("/menu/{id}")
    public void deleteDish(@PathVariable Long id) {
        service.deleteDish(id);
    }

    @PatchMapping("/menu/{id}/availability")
    public VariationDto changeAvailability(@PathVariable Long id, @Valid @RequestBody AvailabilityRequest request) {
        MenuVariation variation = service.changeVariationAvailability(id, request.getAvailable());
        return menuItemMapper.toVariationDto(variation);
    }
}