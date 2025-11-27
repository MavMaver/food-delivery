package dev.marievski.fooddelivery.cart;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CartCalculations {

    public BigDecimal calculateSubtotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getVariation().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int calculateEtaMinutes(Cart cart) {
        int totalQty = cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
        if (totalQty == 0) return 0;

        int weightedCooking = cart.getItems().stream()
                .mapToInt(item -> item.getVariation().getCookingMinutes() * item.getQuantity())
                .sum() / totalQty;

        return weightedCooking + 10;
    }
}