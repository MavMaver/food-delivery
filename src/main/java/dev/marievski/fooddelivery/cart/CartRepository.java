package dev.marievski.fooddelivery.cart;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {"items", "items.variation", "items.variation.item", "user", "restaurant"})
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.active = true")
    Optional<Cart> findByUserIdAndActiveTrue(@Param("userId") Long userId);
}