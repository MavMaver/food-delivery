package dev.marievski.fooddelivery.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Репозиторий заказов.
 * - fetchById: тянет заказ вместе с items и user (для безопасного маппинга в DTO)
 * - findByUserId / findByStatus: используются в OrderService для пагинируемых выборок
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
           select distinct o
           from Order o
           left join fetch o.items i
           left join fetch o.user u
           where o.id = :id
           """)
    Optional<Order> fetchById(@Param("id") Long id);

    // --- методы, которых не хватало ---
    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
