package dev.marievski.fooddelivery.restaurant;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    @EntityGraph(attributePaths = "variations")
    List<MenuItem> findByRestaurantIdOrderByIdAsc(Long restaurantId);
}