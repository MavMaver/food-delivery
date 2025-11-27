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

@Service
public class OrderService {

    private static final BigDecimal MIN_ORDER_TOTAL = BigDecimal.valueOf(300.00);

    private final OrderRepository orders;
    private final CartRepository carts;
    private final CartService cartService;
    private final UserRepository users;

    public OrderService(OrderRepository orders, CartRepository carts,
                        CartService cartService, UserRepository users) {
        this.orders = orders;
        this.carts = carts;
        this.cartService = cartService;
        this.users = users;
    }

    @Transactional
    public Order createFromCart(Long userId) {
        validateUserExists(userId);

        Cart cart = findActiveCart(userId);
        validateCart(cart);

        Order order = buildOrderFromCart(cart);
        order = orders.save(order);

        transferCartItemsToOrder(cart, order);
        deactivateCart(cart);

        return order;
    }

    public Order getOrThrow(Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new ApiBadRequestException("ORDER_NOT_FOUND", "Order not found"));
    }

    public Page<Order> byUser(Long userId, Pageable pageable) {
        return orders.findByUserId(userId, pageable);
    }

    public Page<Order> byStatus(OrderStatus status, Pageable pageable) {
        return orders.findByStatus(status, pageable);
    }

    @Transactional
    public Order changeStatus(Long id, OrderStatus newStatus) {
        Order order = getOrThrow(id);

        if (newStatus == OrderStatus.CANCELLED) {
            return cancelOrder(order);
        }

        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);

        return order;
    }

    @Transactional
    public void cancel(Long id) {
        Order order = getOrThrow(id);
        cancelOrder(order);
    }

    private void validateUserExists(Long userId) {
        users.findById(userId)
                .orElseThrow(() -> new ApiBadRequestException("USER_NOT_FOUND", "User not found"));
    }

    private Cart findActiveCart(Long userId) {
        return carts.findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new ApiBadRequestException("ACTIVE_CART_NOT_FOUND", "Active cart not found"));
    }

    private void validateCart(Cart cart) {
        if (cart.getItems().isEmpty()) {
            throw new ApiConflictException("EMPTY_CART", "Cannot create order without items");
        }

        validateVariationsAvailability(cart);
        validateMinimumTotal(cart);
    }

    private void validateVariationsAvailability(Cart cart) {
        for (CartItem item : cart.getItems()) {
            MenuVariation variation = item.getVariation();
            if (variation == null || !variation.isAvailable()) {
                String label = variation != null ? variation.getLabel() : "unknown";
                throw new ApiConflictException("VARIATION_UNAVAILABLE",
                        "Variation \"" + label + "\" is unavailable");
            }
        }
    }

    private void validateMinimumTotal(Cart cart) {
        BigDecimal total = cartService.subtotal(cart);
        if (total.compareTo(MIN_ORDER_TOTAL) < 0) {
            throw new ApiConflictException("MIN_TOTAL_NOT_REACHED",
                    "Minimum order total is " + MIN_ORDER_TOTAL);
        }
    }

    private Order buildOrderFromCart(Cart cart) {
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setTotal(cartService.subtotal(cart));
        order.setEtaMinutes(cartService.etaMinutes(cart));
        order.setStatus(OrderStatus.NEW);
        return order;
    }

    private void transferCartItemsToOrder(Cart cart, Order order) {
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem(
                    order,
                    cart.getRestaurant(),
                    cartItem.getVariation(),
                    cartItem.getQuantity()
            );
            order.getItems().add(orderItem);
        }
    }

    private void deactivateCart(Cart cart) {
        cart.setActive(false);
    }

    private Order cancelOrder(Order order) {
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new ApiConflictException("CANNOT_CANCEL_DELIVERED",
                    "Delivered order cannot be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return order;
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == next) {
            return; // Идемпотентность - тот же статус
        }

        switch (next) {
            case CONFIRMED -> {
                if (current != OrderStatus.NEW) {
                    throw new ApiConflictException("BAD_TRANSITION",
                            "CONFIRMED allowed only from NEW");
                }
            }
            case READY -> {
                if (current != OrderStatus.CONFIRMED) {
                    throw new ApiConflictException("BAD_TRANSITION",
                            "READY allowed only from CONFIRMED");
                }
            }
            case ASSIGNED -> {
                if (current != OrderStatus.READY) {
                    throw new ApiConflictException("BAD_TRANSITION",
                            "ASSIGNED allowed only from READY");
                }
            }
            case DELIVERING -> {
                if (current != OrderStatus.ASSIGNED) {
                    throw new ApiConflictException("BAD_TRANSITION",
                            "DELIVERING allowed only from ASSIGNED");
                }
            }
            case DELIVERED -> {
                if (current != OrderStatus.DELIVERING) {
                    throw new ApiConflictException("BAD_TRANSITION",
                            "DELIVERED allowed only from DELIVERING");
                }
            }
            case NEW -> {
                throw new ApiConflictException("BAD_TRANSITION",
                        "Cannot return to NEW");
            }
            case CANCELLED -> {
                // Обрабатывается в cancelOrder
            }
        }
    }
}