package dev.marievski.fooddelivery.cart;

import dev.marievski.fooddelivery.restaurant.MenuVariation;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

/**
 * Позиция в корзине: конкретная вариация блюда и количество.
 */
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "variation_id", nullable = false)
    private MenuVariation variation;

    @Min(1)
    @Column(nullable = false)
    private int quantity = 1;

    // --- getters/setters ---
    public Long getId() { return id; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public MenuVariation getVariation() { return variation; }
    public void setVariation(MenuVariation variation) { this.variation = variation; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
