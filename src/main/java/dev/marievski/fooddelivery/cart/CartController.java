package dev.marievski.fooddelivery.cart;

import dev.marievski.fooddelivery.cart.dto.CartDto;
import dev.marievski.fooddelivery.cart.dto.AddItemRequest;
import dev.marievski.fooddelivery.cart.mapper.CartItemMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CartRepository cartRepository;
    private final CartItemMapper cartItemMapper;

    public CartController(CartService cartService, CartRepository cartRepository,
                          CartItemMapper cartItemMapper) {
        this.cartService = cartService;
        this.cartRepository = cartRepository;
        this.cartItemMapper = cartItemMapper;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public CartDto get(@RequestParam("userId") Long userId) {
        return cartRepository.findByUserIdAndActiveTrue(userId)
                .map(this::convertToDto)
                .orElseGet(() -> createEmptyCartDto(userId));
    }

    @PostMapping("/items")
    @Transactional
    public ResponseEntity<CartDto> addItem(@Valid @RequestBody AddItemRequest req) {
        Cart cart = cartService.addItem(req.getUserId(), req.getVariationId(), req.getQuantity());
        CartDto body = convertToDto(cart);
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

    private CartDto createEmptyCartDto(Long userId) {
        CartDto dto = new CartDto();
        dto.setId(null);
        dto.setUserId(userId);
        dto.setRestaurantId(null);
        dto.setSubtotal(java.math.BigDecimal.ZERO);
        dto.setEtaMinutes(0);
        dto.setItems(List.of());
        return dto;
    }

    private CartDto convertToDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getId());
        dto.setRestaurantId(cart.getRestaurant() != null ? cart.getRestaurant().getId() : null);
        dto.setSubtotal(cartService.subtotal(cart));
        dto.setEtaMinutes(cartService.etaMinutes(cart));
        dto.setItems(cart.getItems().stream()
                .map(cartItemMapper::toDto)
                .toList());
        return dto;
    }
}