package dev.marievski.fooddelivery.restaurant;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Вариация блюда: например "маленькая/средняя/большая" с разной ценой и временем готовки.
 * По ТЗ PATCH /menu/{id}/availability — понимать id как id вариации.
 */
@Entity
@Table(name = "menu_variations")
public class MenuVariation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private MenuItem item;

    @NotBlank
    @Column(nullable = false)
    private String label;           // "Small", "Medium", "Large" и т.п.

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Min(1)
    @Column(nullable = false)
    private int cookingMinutes = 10;

    @Column(nullable = false)
    private boolean available = true;

    // --- Геттеры/сеттеры ---
    public Long getId() { return id; }

    public MenuItem getItem() { return item; }
    public void setItem(MenuItem item) { this.item = item; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getCookingMinutes() { return cookingMinutes; }
    public void setCookingMinutes(int cookingMinutes) { this.cookingMinutes = cookingMinutes; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
