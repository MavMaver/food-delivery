package dev.marievski.fooddelivery.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orders;

    public OrderController(OrderService orderService, OrderRepository orders) {
        this.orderService = orderService;
        this.orders = orders;
    }

    // ---------- CREATE ----------

    @PostMapping
    @Transactional
    public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest req) {
        Order order = orderService.createFromCart(req.getUserId());
        // Сразу вернём заголовок Location и DTO; заказ ещё в persistence context.
        return ResponseEntity.ok()
                .location(URI.create("/orders/" + order.getId()))
                .body(toDto(order));
    }

    // ---------- READ ----------

    @GetMapping("/{id}")
    @Transactional(readOnly = true) // держим транзакцию на время маппинга
    public OrderDto get(@PathVariable Long id) {
        // Пробуем жадно вытащить связанные сущности; если вдруг нет — fallback в обычный getOrThrow
        Order o = orders.fetchById(id).orElseGet(() -> orderService.getOrThrow(id));
        return toDto(o);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<OrderDto> list(@RequestParam(value = "userId", required = false) Long userId,
                               @RequestParam(value = "status", required = false) OrderStatus status) {
        if (userId != null) {
            return orderService.byUser(userId, org.springframework.data.domain.Pageable.unpaged())
                    .map(this::toDto).getContent();
        }
        if (status != null) {
            return orderService.byStatus(status, org.springframework.data.domain.Pageable.unpaged())
                    .map(this::toDto).getContent();
        }
        // Для общего списка нам fetch-join не критичен, маппим только базовые поля
        return orders.findAll().stream().map(this::toDto).toList();
    }

    // ---------- STATUS ----------

    @PatchMapping("/{id}/status")
    @Transactional
    public OrderDto changeStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest req) {
        // Меняем статус бизнес-методом...
        orderService.changeStatus(id, req.getStatus());
        // ...и читаем обратно ИМЕННО через fetch-метод, чтобы безопасно собрать DTO
        Order fresh = orders.fetchById(id).orElseGet(() -> orderService.getOrThrow(id));
        return toDto(fresh);
    }

    // ---------- CANCEL ----------

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return ResponseEntity.ok().build();
    }

    // ---------- DTO ----------

    public static final class CreateOrderRequest {
        @NotNull
        private Long userId;
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }

    public static final class StatusUpdateRequest {
        @NotNull
        private OrderStatus status;
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
    }

    public static final class OrderDto {
        public Long id;
        public Long userId;
        public BigDecimal total;
        public Integer etaMinutes;
        public String status;
        public List<OrderItemDto> items;
    }

    public static final class OrderItemDto {
        public Long id;
        public Integer quantity;
        // Оставляем поля под вариацию — если когда-нибудь добавим её в сущность
        public Long variationId;
        public String label;
        public BigDecimal price;
        public BigDecimal lineTotal;
    }

    // ---------- MAPPING ----------

    private OrderDto toDto(Order o) {
        OrderDto dto = new OrderDto();
        dto.id = o.getId();
        dto.userId = o.getUser() != null ? o.getUser().getId() : null;
        dto.total = o.getTotal();
        dto.etaMinutes = o.getEtaMinutes();
        dto.status = o.getStatus() != null ? o.getStatus().name() : null;

        dto.items = o.getItems() == null ? List.of() : o.getItems().stream().map(oi -> {
            OrderItemDto i = new OrderItemDto();
            i.id = oi.getId();
            i.quantity = oi.getQuantity();
            // Остальные поля по вариации не трогаем (их может не быть в модели)
            return i;
        }).toList();

        return dto;
    }
}
