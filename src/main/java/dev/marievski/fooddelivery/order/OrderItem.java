package dev.marievski.fooddelivery.order;

import dev.marievski.fooddelivery.restaurant.MenuVariation;
import dev.marievski.fooddelivery.restaurant.Restaurant;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

/**
 * Позиция заказа. Сохраняем "снимок" важных полей, чтобы история не зависела от изменений меню:
 * - название блюда и вариации,
 * - цену и время готовки,
 * - ресторан.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private String variationLabel;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Min(1)
    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int cookingMinutes;

    public OrderItem() {}

    /** Удобный конструктор из вариации, когда переносим позиции из корзины. */
    public OrderItem(Order order, Restaurant restaurant, MenuVariation v, int quantity) {
        this.order = order;
        this.restaurant = restaurant;
        this.itemName = v.getItem().getName();
        this.variationLabel = v.getLabel();
        this.price = v.getPrice();
        this.quantity = quantity;
        this.cookingMinutes = v.getCookingMinutes();
    }

    // --- getters ---
    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public Restaurant getRestaurant() { return restaurant; }
    public String getItemName() { return itemName; }
    public String getVariationLabel() { return variationLabel; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public int getCookingMinutes() { return cookingMinutes; }
}
