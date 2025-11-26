package dev.marievski.fooddelivery.cart;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /** Нужен, чтобы не плодить дубликаты строк на одну и ту же вариацию. */
    Optional<CartItem> findByCartIdAndVariationId(Long cartId, Long variationId);
}
