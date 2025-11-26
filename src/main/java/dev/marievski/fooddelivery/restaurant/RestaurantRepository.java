package dev.marievski.fooddelivery.restaurant;

import dev.marievski.fooddelivery.common.Cuisine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий ресторанов. Набор методов под разные комбинации фильтров.
 */
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Page<Restaurant> findByCuisine(Cuisine cuisine, Pageable pageable);

    Page<Restaurant> findByRatingGreaterThanEqual(double rating, Pageable pageable);

    Page<Restaurant> findByCuisineAndRatingGreaterThanEqual(Cuisine cuisine, double rating, Pageable pageable);
}
