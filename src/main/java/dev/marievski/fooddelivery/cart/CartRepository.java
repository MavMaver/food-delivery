package dev.marievski.fooddelivery.cart;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    /** Активная корзина пользователя (по правилу п.61 всегда максимум одна). */
    Optional<Cart> findByUserIdAndActiveTrue(Long userId);
}
