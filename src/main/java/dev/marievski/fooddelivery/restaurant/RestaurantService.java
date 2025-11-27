package dev.marievski.fooddelivery.restaurant;

import dev.marievski.fooddelivery.common.Cuisine;
import dev.marievski.fooddelivery.common.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurants;
    private final MenuItemRepository items;
    private final MenuVariationRepository variations;

    public RestaurantService(RestaurantRepository restaurants,
                             MenuItemRepository items,
                             MenuVariationRepository variations) {
        this.restaurants = restaurants;
        this.items = items;
        this.variations = variations;
    }

    public Restaurant create(Restaurant r) {
        return restaurants.save(r);
    }

    public Restaurant getOrThrow(Long id) {
        return restaurants.findById(id).orElseThrow(() -> new NotFoundException("Ресторан не найден"));
    }

    @Transactional
    public Restaurant update(Long id, Restaurant patch) {
        Restaurant r = getOrThrow(id);
        if (patch.getName() != null) r.setName(patch.getName());
        if (patch.getCuisine() != null) r.setCuisine(patch.getCuisine());
        r.setOpen(patch.isOpen());
        return r;
    }

    @Transactional
    public void close(Long id) {
        Restaurant r = getOrThrow(id);
        r.setOpen(false);
    }

    public Page<Restaurant> list(Cuisine cuisine, Double minRating, Pageable pageable) {
        if (cuisine != null && minRating != null) {
            return restaurants.findByCuisineAndRatingGreaterThanEqual(cuisine, minRating, pageable);
        } else if (cuisine != null) {
            return restaurants.findByCuisine(cuisine, pageable);
        } else if (minRating != null) {
            return restaurants.findByRatingGreaterThanEqual(minRating, pageable);
        }
        return restaurants.findAll(pageable);
    }

    @Transactional
    public MenuItem addDish(Long restaurantId, String name, String description, List<MenuVariation> newVariations) {
        Restaurant r = getOrThrow(restaurantId);

        MenuItem item = new MenuItem();
        item.setRestaurant(r);
        item.setName(name);
        item.setDescription(description);
        items.save(item);

        for (MenuVariation v : newVariations) {
            v.setItem(item);
            MenuVariation savedVar = variations.save(v);
            item.getVariations().add(savedVar);
        }

        return item;
    }

    public List<MenuItem> menuOf(Long restaurantId) {
        return items.findByRestaurantIdOrderByIdAsc(restaurantId);
    }

    @Transactional
    public MenuItem updateDish(Long itemId, String name, String description, Boolean active) {
        MenuItem item = items.findById(itemId).orElseThrow(() -> new NotFoundException("Блюдо не найдено"));
        if (name != null) item.setName(name);
        if (description != null) item.setDescription(description);
        if (active != null) item.setActive(active);
        return item;
    }

    public void deleteDish(Long itemId) {
        items.deleteById(itemId);
    }

    @Transactional
    public MenuVariation changeVariationAvailability(Long variationId, boolean available) {
        MenuVariation v = variations.findById(variationId)
                .orElseThrow(() -> new NotFoundException("Вариация не найдена"));
        v.setAvailable(available);
        return v;
    }
}