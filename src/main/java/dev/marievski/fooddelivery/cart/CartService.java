package dev.marievski.fooddelivery.cart;

import dev.marievski.fooddelivery.restaurant.MenuVariation;
import dev.marievski.fooddelivery.restaurant.MenuVariationRepository;
import dev.marievski.fooddelivery.restaurant.Restaurant;
import dev.marievski.fooddelivery.user.User;
import dev.marievski.fooddelivery.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Бизнес-логика корзины.
 * Правила:
 *  - один активный Cart на пользователя (создаём при первом обращении);
 *  - добавлять можно только вариации одного ресторана (п.51);
 *  - если вариация недоступна — запрещаем;
 *  - если ресторан закрыт — запрещаем (п.60);
 *  - ETA считаем как средневзвешенное время готовки + "нагрузка курьеров".
 */
@Service
public class CartService {

    private final CartRepository carts;
    private final CartItemRepository items;
    private final MenuVariationRepository variations;
    private final UserRepository users;

    public CartService(CartRepository carts,
                       CartItemRepository items,
                       MenuVariationRepository variations,
                       UserRepository users) {
        this.carts = carts;
        this.items = items;
        this.variations = variations;
        this.users = users;
    }

    /** Найти активную корзину пользователя или создать новую (п.61). */
    @Transactional
    public Cart getOrCreateActiveCart(Long userId) {
        return carts.findByUserIdAndActiveTrue(userId)
                .orElseGet(() -> {
                    User u = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
                    Cart c = new Cart();
                    c.setUser(u);
                    return carts.save(c);
                });
    }

    /** Добавить вариацию блюда в корзину (проверяем ресторан и доступность). */
    @Transactional
    public Cart addItem(Long userId, Long variationId, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Количество должно быть положительным");

        Cart cart = getOrCreateActiveCart(userId);
        MenuVariation v = variations.findById(variationId)
                .orElseThrow(() -> new IllegalArgumentException("Вариация не найдена"));

        if (!v.isAvailable()) {
            throw new IllegalStateException("Вариация недоступна для заказа");
        }

        Restaurant restaurantOfVariation = v.getItem().getRestaurant();
        if (!restaurantOfVariation.isOpen()) {
            // Новое правило (п.60)
            throw new IllegalStateException("Ресторан закрыт: нельзя добавлять блюда (п.60)");
        }

        if (cart.getRestaurant() == null) {
            cart.setRestaurant(restaurantOfVariation); // привязываем корзину к ресторану первой позиции
        } else if (!cart.getRestaurant().getId().equals(restaurantOfVariation.getId())) {
            // п.51 — корзина только одного ресторана
            throw new IllegalStateException("Корзина может содержать блюда только из одного ресторана");
        }

        // Если уже есть такая вариация — увеличиваем количество, иначе добавляем новую позицию
        CartItem existing = items.findByCartIdAndVariationId(cart.getId(), variationId).orElse(null);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + qty);
        } else {
            CartItem ci = new CartItem();
            ci.setCart(cart);
            ci.setVariation(v);
            ci.setQuantity(qty);
            items.save(ci);
        }
        return cart;
    }

    /** Изменить количество позиции. qty<=0 -> удаление. */
    @Transactional
    public Cart updateItemQuantity(Long itemId, int qty) {
        CartItem it = items.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Позиция корзины не найдена"));
        if (qty <= 0) {
            Cart c = it.getCart();
            items.delete(it);
            if (c.getItems().isEmpty()) c.setRestaurant(null);
            return c;
        }
        it.setQuantity(qty);
        return it.getCart();
    }

    /** Удалить позицию. */
    @Transactional
    public Cart removeItem(Long itemId) {
        CartItem it = items.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Позиция корзины не найдена"));
        Cart c = it.getCart();
        items.delete(it);
        if (c.getItems().isEmpty()) c.setRestaurant(null);
        return c;
    }

    /** Очистить корзину. */
    @Transactional
    public void clear(Long userId) {
        Cart c = getOrCreateActiveCart(userId);
        c.getItems().clear(); // благодаря orphanRemoval позиции удалятся
        c.setRestaurant(null);
    }

    /** Подсчитать текущую сумму корзины. */
    public BigDecimal subtotal(Cart cart) {
        return cart.getItems().stream()
                .map(i -> i.getVariation().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Примерная оценка ETA:
     *  - средневзвешенное время готовки по позициям корзины;
     *  - +10 минут "нагрузка курьеров"
     */
    public int etaMinutes(Cart cart) {
        int totalQty = cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
        if (totalQty == 0) return 0;
        int weightedCooking = cart.getItems().stream()
                .mapToInt(i -> i.getVariation().getCookingMinutes() * i.getQuantity())
                .sum() / totalQty;
        int courierLoad = 10; // можно улучшить, учитывая активные заказы
        return weightedCooking + courierLoad;
    }
}
