package dev.marievski.fooddelivery.restaurant;

import dev.marievski.fooddelivery.common.Cuisine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Бизнес-логика ресторанов и меню.
 * Разделяю ответственность: RestaurantService отвечает за ресторан и блюда,
 * а, например, "Отзывы" будут отдельным модулем, который при изменении рейтинга
 * вызовет setRating в Restaurant.
 */
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

    // --- Рестораны ---

    public Restaurant create(Restaurant r) {
        return restaurants.save(r);
    }

    public Restaurant getOrThrow(Long id) {
        return restaurants.findById(id).orElseThrow(() -> new IllegalArgumentException("Ресторан не найден"));
    }

    @Transactional
    public Restaurant update(Long id, Restaurant patch) {
        Restaurant r = getOrThrow(id);
        if (patch.getName() != null) r.setName(patch.getName());
        if (patch.getCuisine() != null) r.setCuisine(patch.getCuisine());
        r.setOpen(patch.isOpen());
        // rating по ТЗ считается из отзывов — ручное изменение здесь не даём
        return r;
    }

    /** "Закрыть ресторан" по ТЗ. */
    @Transactional
    public void close(Long id) {
        Restaurant r = getOrThrow(id);
        r.setOpen(false);
    }

    /** Список с фильтрами по кухне и рейтингу. */
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

    // --- Меню ---

    /**
     * Добавить блюдо с вариациями в ресторан.
     * ВАЖНО: сразу наполняем item.getVariations(), чтобы клиент получил ID вариаций в ответе.
     */
    @Transactional
    public MenuItem addDish(Long restaurantId, String name, String description, List<MenuVariation> newVariations) {
        Restaurant r = getOrThrow(restaurantId);

        MenuItem item = new MenuItem();
        item.setRestaurant(r);
        item.setName(name);
        item.setDescription(description);
        items.save(item); // создаём блюдо

        for (MenuVariation v : newVariations) {
            v.setItem(item);
            MenuVariation savedVar = variations.save(v); // сохраняем вариацию
            // ключевая строка: добавляем сохранённую вариацию в коллекцию блюда
            item.getVariations().add(savedVar);
        }

        // item — управляемая сущность; список уже наполнен, сериализация отдаст variations с id
        return item;
    }

    /** Список блюд ресторана (с вариациями подтянутся лениво при маппинге в DTO). */
    public List<MenuItem> menuOf(Long restaurantId) {
        return items.findByRestaurantIdOrderByIdAsc(restaurantId);
    }

    /** Обновить блюдо (имя/описание/активность). */
    @Transactional
    public MenuItem updateDish(Long itemId, String name, String description, Boolean active) {
        MenuItem item = items.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Блюдо не найдено"));
        if (name != null) item.setName(name);
        if (description != null) item.setDescription(description);
        if (active != null) item.setActive(active);
        return item;
    }

    /** Удалить блюдо (каскадно удалятся вариации). */
    public void deleteDish(Long itemId) {
        items.deleteById(itemId);
    }

    /** PATCH доступности вариации по ТЗ: /menu/{id}/availability */
    @Transactional
    public MenuVariation changeVariationAvailability(Long variationId, boolean available) {
        MenuVariation v = variations.findById(variationId)
                .orElseThrow(() -> new IllegalArgumentException("Вариация не найдена"));
        v.setAvailable(available);
        return v;
    }
}
