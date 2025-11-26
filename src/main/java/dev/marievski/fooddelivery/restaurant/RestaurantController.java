package dev.marievski.fooddelivery.restaurant;

import dev.marievski.fooddelivery.common.Cuisine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST API рестораны и меню.
 * DTO-шки объявляю как record'ы прямо в контроллере — быстро и читаемо.
 */
@RestController
public class RestaurantController {

    private final RestaurantService service;

    public RestaurantController(RestaurantService service) {
        this.service = service;
    }

    // ---------- DTO ----------

    /** Создание/обновление ресторана. */
    public record RestaurantRq(
            @NotBlank String name,
            @NotNull Cuisine cuisine,
            Boolean open
    ) {}

    /** Ответ по ресторану. */
    public record RestaurantDto(Long id, String name, Cuisine cuisine, boolean open, double rating) {
        public static RestaurantDto of(Restaurant r) {
            return new RestaurantDto(r.getId(), r.getName(), r.getCuisine(), r.isOpen(), r.getRating());
        }
    }

    /** DTO вариации блюда при создании. */
    public record VariationRq(
            @NotBlank String label,
            @NotNull BigDecimal price,
            @Min(1) int cookingMinutes,
            Boolean available
    ) {}

    /** DTO для блюда с вариациями (ответ). */
    public record VariationDto(Long id, String label, BigDecimal price, int cookingMinutes, boolean available) {
        public static VariationDto of(MenuVariation v) {
            return new VariationDto(v.getId(), v.getLabel(), v.getPrice(), v.getCookingMinutes(), v.isAvailable());
        }
    }

    public record MenuItemCreateRq(
            @NotBlank String name,
            String description,
            @NotNull List<@Valid VariationRq> variations // список вариаций обязателен
    ) {}

    public record MenuItemUpdateRq(
            String name,
            String description,
            Boolean active
    ) {}

    public record MenuItemDto(Long id, String name, String description, boolean active, List<VariationDto> variations) {
        public static MenuItemDto of(MenuItem item) {
            List<VariationDto> vars = item.getVariations().stream().map(VariationDto::of).toList();
            return new MenuItemDto(item.getId(), item.getName(), item.getDescription(), item.isActive(), vars);
        }
    }

    // ---------- Рестораны ----------

    /** POST /restaurants — создать ресторан. */
    @PostMapping("/restaurants")
    public RestaurantDto createRestaurant(@Valid @RequestBody RestaurantRq rq) {
        Restaurant r = new Restaurant();
        r.setName(rq.name());
        r.setCuisine(rq.cuisine());
        r.setOpen(rq.open == null ? true : rq.open());
        return RestaurantDto.of(service.create(r));
    }

    /** GET /restaurants — список с фильтрами по кухне и рейтингу. */
    @GetMapping("/restaurants")
    public Page<RestaurantDto> listRestaurants(@RequestParam(required = false) Cuisine cuisine,
                                               @RequestParam(required = false) Double minRating,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return service.list(cuisine, minRating, PageRequest.of(page, size)).map(RestaurantDto::of);
    }

    /** GET /restaurants/{id} — детально. */
    @GetMapping("/restaurants/{id}")
    public RestaurantDto getRestaurant(@PathVariable Long id) {
        try {
            return RestaurantDto.of(service.getOrThrow(id));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /** PUT /restaurants/{id} — обновить ресторан. */
    @PutMapping("/restaurants/{id}")
    public RestaurantDto updateRestaurant(@PathVariable Long id, @Valid @RequestBody RestaurantRq rq) {
        Restaurant patch = new Restaurant();
        patch.setName(rq.name());
        patch.setCuisine(rq.cuisine());
        patch.setOpen(rq.open == null ? true : rq.open());
        try {
            return RestaurantDto.of(service.update(id, patch));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /** DELETE /restaurants/{id} — закрыть ресторан. */
    @DeleteMapping("/restaurants/{id}")
    public void deleteRestaurant(@PathVariable Long id) {
        try {
            service.close(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // ---------- Меню ----------

    /** POST /restaurants/{id}/menu — добавить блюдо с вариациями. */
    @PostMapping("/restaurants/{id}/menu")
    public MenuItemDto addDish(@PathVariable Long id, @Valid @RequestBody MenuItemCreateRq rq) {
        List<MenuVariation> vars = rq.variations().stream().map(v -> {
            MenuVariation mv = new MenuVariation();
            mv.setLabel(v.label());
            mv.setPrice(v.price());
            mv.setCookingMinutes(v.cookingMinutes());
            mv.setAvailable(v.available() == null ? true : v.available());
            return mv;
        }).toList();

        try {
            MenuItem saved = service.addDish(id, rq.name(), rq.description(), vars);
            return MenuItemDto.of(saved);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /** GET /restaurants/{id}/menu — список блюд ресторана. */
    @GetMapping("/restaurants/{id}/menu")
    public List<MenuItemDto> menu(@PathVariable Long id) {
        return service.menuOf(id).stream().map(MenuItemDto::of).toList();
    }

    /** PUT /menu/{id} — обновить блюдо (не вариацию). */
    @PutMapping("/menu/{id}")
    public MenuItemDto updateDish(@PathVariable Long id, @Valid @RequestBody MenuItemUpdateRq rq) {
        try {
            MenuItem item = service.updateDish(id, rq.name(), rq.description(), rq.active());
            return MenuItemDto.of(item);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /** DELETE /menu/{id} — удалить блюдо. */
    @DeleteMapping("/menu/{id}")
    public void deleteDish(@PathVariable Long id) {
        service.deleteDish(id);
    }

    /**
     * PATCH /menu/{id}/availability — изменить доступность вариации.
     * По ТЗ endpoint называется "блюда", но для гибкости понимаем id как id вариации.
     */
    public record AvailabilityRq(@NotNull Boolean available) {}

    @PatchMapping("/menu/{id}/availability")
    public VariationDto changeAvailability(@PathVariable Long id, @Valid @RequestBody AvailabilityRq rq) {
        try {
            return VariationDto.of(service.changeVariationAvailability(id, rq.available()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
