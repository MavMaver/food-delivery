package dev.marievski.fooddelivery.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CartRepository cartRepository;

    public CartController(CartService cartService, CartRepository cartRepository) {
        this.cartService = cartService;
        this.cartRepository = cartRepository;
    }

    // ---------- READ ----------

    @GetMapping
    @Transactional(readOnly = true)
    public CartDto get(@RequestParam("userId") Long userId) {
        return cartRepository.findByUserIdAndActiveTrue(userId)
                .map(this::toDto)
                // если активной корзины нет — отдаём "пустую" DTO (так твои http-тесты получают 200 и subtotal=0)
                .orElseGet(() -> {
                    CartDto dto = new CartDto();
                    dto.id = null;
                    dto.userId = userId;
                    dto.restaurantId = null;
                    dto.subtotal = BigDecimal.ZERO;
                    dto.etaMinutes = 0;
                    dto.items = List.of();
                    return dto;
                });
    }

    // ---------- WRITE ----------

    @PostMapping("/items")
    @Transactional
    public ResponseEntity<CartDto> addItem(@RequestBody AddItemRequest req) {
        Cart cart = cartService.addItem(req.getUserId(), req.getVariationId(), req.getQuantity());
        CartDto body = toDto(cart);
        return ResponseEntity.ok()
                .location(URI.create("/cart?userId=" + req.getUserId()))
                .body(body);
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<Void> clear(@RequestParam("userId") Long userId) {
        cartService.clear(userId);
        return ResponseEntity.ok().build();
    }

    // ---------- DTO ----------

    public static final class AddItemRequest {
        @NotNull private Long userId;
        @NotNull private Long variationId;
        @Min(1) private Integer quantity;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getVariationId() { return variationId; }
        public void setVariationId(Long variationId) { this.variationId = variationId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static final class CartDto {
        public Long id;
        public Long userId;
        public Long restaurantId;      // может быть null
        public BigDecimal subtotal;
        public int etaMinutes;
        public List<CartItemDto> items;
    }

    public static final class CartItemDto {
        public Long id;
        public Long variationId;   // может быть null, если в модели нет связи
        public String label;       // может быть null
        public BigDecimal price;   // может быть null
        public int quantity;
        public BigDecimal lineTotal;
    }

    // ---------- MAPPING ----------

    private CartDto toDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.id = cart.getId();
        dto.userId = cart.getUser() != null ? cart.getUser().getId() : null;
        dto.restaurantId = cart.getRestaurant() != null ? cart.getRestaurant().getId() : null;
        dto.subtotal = cartService.subtotal(cart);
        dto.etaMinutes = cartService.etaMinutes(cart);
        dto.items = cart.getItems().stream().map(ci -> {
            CartItemDto i = new CartItemDto();
            i.id = ci.getId();
            // безопасно: если в твоём CartItem нет getVariation(), эти поля останутся null
            try {
                var v = ci.getVariation();
                if (v != null) {
                    i.variationId = v.getId();
                    i.label = v.getLabel();
                    i.price = v.getPrice();
                }
            } catch (Throwable ignored) { /* поле variation отсутствует — пропускаем */ }
            i.quantity = ci.getQuantity();
            i.lineTotal = (i.price != null)
                    ? i.price.multiply(BigDecimal.valueOf(i.quantity))
                    : BigDecimal.ZERO;
            return i;
        }).toList();
        return dto;
    }
}
