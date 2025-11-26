package dev.marievski.fooddelivery.restaurant;

import dev.marievski.fooddelivery.common.Cuisine;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Ресторан.
 * Важные моменты:
 *  - rating храню в ресторане (считается из отзывов позже);
 *  - open = открыт/закрыт (DELETE по ТЗ — "закрыть ресторан");
 *  - LAZY на коллекциях; DTO в контроллере исключают рекурсию.
 */
@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Cuisine cuisine = Cuisine.OTHER;

    @Column(nullable = false)
    private boolean open = true;

    @Column(nullable = false)
    private double rating = 0.0;           // фильтрация по рейтингу по ТЗ

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MenuItem> menu = new ArrayList<>();

    // --- Геттеры/сеттеры ---
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Cuisine getCuisine() { return cuisine; }
    public void setCuisine(Cuisine cuisine) { this.cuisine = cuisine; }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public Instant getCreatedAt() { return createdAt; }

    public List<MenuItem> getMenu() { return menu; }
}
