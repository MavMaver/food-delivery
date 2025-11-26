package dev.marievski.fooddelivery.payment;

import dev.marievski.fooddelivery.common.ApiBadRequestException;
import dev.marievski.fooddelivery.common.ApiConflictException;
import dev.marievski.fooddelivery.order.Order;
import dev.marievski.fooddelivery.order.OrderRepository;
import dev.marievski.fooddelivery.order.OrderService;
import dev.marievski.fooddelivery.order.OrderStatus;
import dev.marievski.fooddelivery.payment.dto.PaymentCreateRequest;
import dev.marievski.fooddelivery.payment.dto.PaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository payments;
    private final OrderRepository orders;
    private final OrderService orderService;

    public PaymentService(PaymentRepository payments, OrderRepository orders, OrderService orderService) {
        this.payments = payments;
        this.orders = orders;
        this.orderService = orderService;
    }

    @Transactional
    public PaymentResponse create(PaymentCreateRequest req) {
        if (req.getOrderId() == null || req.getOrderId() <= 0) {
            throw new ApiBadRequestException("BAD_ORDER_ID", "orderId must be > 0");
        }
        if (req.getAmount() == null || req.getAmount().compareTo(new BigDecimal("0.00")) <= 0) {
            throw new ApiBadRequestException("BAD_AMOUNT", "amount must be > 0");
        }

        // идемпотентность по externalId
        if (req.getExternalId() != null) {
            payments.findByExternalId(req.getExternalId()).ifPresent(p -> {
                throw new ApiConflictException("DUPLICATE_EXTERNAL_ID", "Payment with this externalId already exists");
            });
        }

        Order order = orders.findById(req.getOrderId())
                .orElseThrow(() -> new ApiBadRequestException("ORDER_NOT_FOUND", "Order not found"));

        if (order.getStatus() != OrderStatus.NEW) {
            throw new ApiConflictException("ORDER_NOT_NEW", "Order must be NEW to create payment");
        }
        if (order.getTotal() == null) {
            throw new ApiConflictException("ORDER_TOTAL_MISSING", "Order has no total");
        }

        // 30) «wrong amount» -> 409
        if (order.getTotal().compareTo(req.getAmount()) != 0) {
            throw new ApiConflictException("AMOUNT_MISMATCH", "Amount doesn't match order total");
        }

        Payment p = new Payment();
        p.setOrder(order);
        p.setAmount(req.getAmount());
        p.setStatus(PaymentStatus.PENDING);
        p.setExternalId(req.getExternalId());
        p.setUpdatedAt(Instant.now());
        payments.save(p);

        return toDto(p);
    }

    @Transactional
    public PaymentResponse updateStatus(Long id, PaymentStatus newStatus) {
        Payment p = payments.findById(id)
                .orElseThrow(() -> new ApiBadRequestException("PAYMENT_NOT_FOUND", "Payment not found"));

        if (p.getStatus() == newStatus) {
            return toDto(p);
        }

        p.setStatus(newStatus);
        p.setUpdatedAt(Instant.now());

        // Бизнес-эффект на заказ
        Order o = p.getOrder();
        if (newStatus == PaymentStatus.SUCCESS) {
            if (o.getStatus() == OrderStatus.NEW) {
                orderService.changeStatus(o.getId(), OrderStatus.CONFIRMED);
            }
        } else if (newStatus == PaymentStatus.FAILED) {
            // 33–34) при провале — отменяем заказ
            if (o.getStatus() != OrderStatus.DELIVERED && o.getStatus() != OrderStatus.CANCELLED) {
                orderService.changeStatus(o.getId(), OrderStatus.CANCELLED);
            }
        }

        return toDto(p);
    }

    public List<PaymentResponse> findByOrderId(Long orderId) {
        return payments.findByOrderId(orderId).stream().map(this::toDto).toList();
    }

    private PaymentResponse toDto(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getOrder().getId(),
                p.getAmount(),
                p.getStatus(),
                p.getExternalId(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

}

