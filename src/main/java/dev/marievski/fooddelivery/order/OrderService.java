package dev.marievski.fooddelivery.order;

import dev.marievski.fooddelivery.cart.Cart;
import dev.marievski.fooddelivery.cart.CartItem;
import dev.marievski.fooddelivery.cart.CartRepository;
import dev.marievski.fooddelivery.cart.CartService;
import dev.marievski.fooddelivery.common.ApiBadRequestException;
import dev.marievski.fooddelivery.common.ApiConflictException;
import dev.marievski.fooddelivery.restaurant.MenuVariation;
import dev.marievski.fooddelivery.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Бизнес-логика заказов.
 */
@Service
public class OrderService {

    private static final BigDecimal MIN_ORDER_TOTAL = BigDecimal.valueOf(300); // п.52

    private final OrderRepository orders;
    private final CartRepository carts;
    private final CartService cartService;
    private final UserRepository users;

    public OrderService(OrderRepository orders,
                        CartRepository carts,
                        CartService cartService,
                        UserRepository users) {
        this.orders = orders;
        this.carts = carts;
        this.cartService = cartService;
        this.users = users;
    }

    /** Создание заказа из активной корзины пользователя. */
    @Transactional
    public Order createFromCart(Long userId) {
        // существование пользователя -> 400
        users.findById(userId).orElseThrow(() ->
                new ApiBadRequestException("USER_NOT_FOUND", "User not found"));

        Cart cart = carts.findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new ApiBadRequestException("ACTIVE_CART_NOT_FOUND", "Active cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new ApiConflictException("EMPTY_CART", "Cannot create order without items");
        }

        // Проверяем доступность вариаций (п.66) -> 409
        for (CartItem i : cart.getItems()) {
            MenuVariation v = i.getVariation();
            if (v == null || !v.isAvailable()) {
                String label = (v != null ? v.getLabel() : "?");
                throw new ApiConflictException("VARIATION_UNAVAILABLE",
                        "Variation \"" + label + "\" is unavailable");
            }
        }

        // Считаем сумму и проверяем минимум (п.52) -> 409
        BigDecimal total = cartService.subtotal(cart);
        if (total.compareTo(MIN_ORDER_TOTAL) < 0) {
            throw new ApiConflictException("MIN_TOTAL_NOT_REACHED", "Minimum order total is 300");
        }

        // Создаём заказ и копируем позиции
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setTotal(total);
        order.setEtaMinutes(cartService.etaMinutes(cart));
        order = orders.save(order);

        for (CartItem i : cart.getItems()) {
            order.getItems().add(new OrderItem(order, i.getCart().getRestaurant(), i.getVariation(), i.getQuantity()));
        }

        // Закрываем корзину (п.61)
        cart.setActive(false);
        return order;
    }

    /** Получить заказ или 400, если не найден. */
    public Order getOrThrow(Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new ApiBadRequestException("ORDER_NOT_FOUND", "Order not found"));
    }

    /** Список заказов пользователя. */
    public Page<Order> byUser(Long userId, Pageable pageable) {
        return orders.findByUserId(userId, pageable);
    }

    /** Список по статусу. */
    public Page<Order> byStatus(OrderStatus status, Pageable pageable) {
        return orders.findByStatus(status, pageable);
    }

    /**
     * Изменение статуса.
     * Валидация переходов; конфликтные случаи -> 409
     */
    @Transactional
    public Order changeStatus(Long id, OrderStatus newStatus) {
        Order o = getOrThrow(id);
        OrderStatus cur = o.getStatus();

        if (newStatus == OrderStatus.CANCELLED) {
            if (cur == OrderStatus.DELIVERED) {
                throw new ApiConflictException("CANNOT_CANCEL_DELIVERED", "Delivered order cannot be cancelled");
            }
            o.setStatus(OrderStatus.CANCELLED);
            return o;
        }

        switch (newStatus) {
            case READY -> {
                if (cur != OrderStatus.CONFIRMED)
                    throw new ApiConflictException("BAD_TRANSITION", "READY allowed only from CONFIRMED");
                o.setStatus(OrderStatus.READY);
            }
            case ASSIGNED -> {
                if (cur != OrderStatus.READY)
                    throw new ApiConflictException("BAD_TRANSITION", "ASSIGNED allowed only from READY");
                o.setStatus(OrderStatus.ASSIGNED);
            }
            case DELIVERING -> {
                if (cur != OrderStatus.ASSIGNED)
                    throw new ApiConflictException("BAD_TRANSITION", "DELIVERING allowed only from ASSIGNED");
                o.setStatus(OrderStatus.DELIVERING);
            }
            case DELIVERED -> {
                if (cur != OrderStatus.DELIVERING)
                    throw new ApiConflictException("BAD_TRANSITION", "DELIVERED allowed only from DELIVERING");
                o.setStatus(OrderStatus.DELIVERED);
            }
            case CONFIRMED -> {
                if (cur != OrderStatus.NEW)
                    throw new ApiConflictException("BAD_TRANSITION", "CONFIRMED allowed only from NEW");
                o.setStatus(OrderStatus.CONFIRMED);
            }
            case NEW -> throw new ApiConflictException("BAD_TRANSITION", "Cannot return to NEW");
        }
        return o;
    }

    /** Отмена заказа. */
    @Transactional
    public void cancel(Long id) {
        changeStatus(id, OrderStatus.CANCELLED);
    }
}
