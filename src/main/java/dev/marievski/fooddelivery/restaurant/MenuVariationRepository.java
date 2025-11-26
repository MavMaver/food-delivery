package dev.marievski.fooddelivery.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuVariationRepository extends JpaRepository<MenuVariation, Long> {

    List<MenuVariation> findByItemIdOrderByIdAsc(Long itemId);
}
