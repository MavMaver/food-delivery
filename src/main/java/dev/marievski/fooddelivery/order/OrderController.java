package dev.marievski.fooddelivery.order;

import dev.marievski.fooddelivery.order.dto.CreateOrderRequest;
import dev.marievski.fooddelivery.order.dto.OrderDto;
import dev.marievski.fooddelivery.order.dto.StatusUpdateRequest;
import dev.marievski.fooddelivery.order.mapper.OrderMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orders;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderRepository orders, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orders = orders;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest req) {
        Order order = orderService.createFromCart(req.getUserId());
        OrderDto body = orderMapper.toDto(order);
        return ResponseEntity.ok()
                .location(URI.create("/orders/" + order.getId()))
                .body(body);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public OrderDto get(@PathVariable Long id) {
        Order order = orders.fetchById(id)
                .orElseGet(() -> orderService.getOrThrow(id));
        return orderMapper.toDto(order);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<OrderDto> list(@RequestParam(value = "userId", required = false) Long userId,
                               @RequestParam(value = "status", required = false) OrderStatus status) {
        if (userId != null) {
            Page<Order> userOrders = orderService.byUser(userId, Pageable.unpaged());
            return orderMapper.toDtoList(userOrders.getContent());
        }
        if (status != null) {
            Page<Order> statusOrders = orderService.byStatus(status, Pageable.unpaged());
            return orderMapper.toDtoList(statusOrders.getContent());
        }
        return orderMapper.toDtoList(orders.findAll());
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public OrderDto changeStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest req) {
        orderService.changeStatus(id, req.getStatus());
        Order fresh = orders.fetchById(id)
                .orElseGet(() -> orderService.getOrThrow(id));
        return orderMapper.toDto(fresh);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return ResponseEntity.ok().build();
    }
}