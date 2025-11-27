package dev.marievski.fooddelivery.payment;

import dev.marievski.fooddelivery.common.ApiBadRequestException;
import dev.marievski.fooddelivery.common.ApiConflictException;
import dev.marievski.fooddelivery.order.Order;
import dev.marievski.fooddelivery.order.OrderRepository;
import dev.marievski.fooddelivery.order.OrderService;
import dev.marievski.fooddelivery.order.OrderStatus;
import dev.marievski.fooddelivery.payment.dto.PaymentCreateRequest;
import dev.marievski.fooddelivery.payment.dto.PaymentResponse;
import dev.marievski.fooddelivery.payment.mapper.PaymentMapper;
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
    private final PaymentMapper paymentMapper;

    public PaymentService(PaymentRepository payments, OrderRepository orders,
                          OrderService orderService, PaymentMapper paymentMapper) {
        this.payments = payments;
        this.orders = orders;
        this.orderService = orderService;
        this.paymentMapper = paymentMapper;
    }

    @Transactional
    public PaymentResponse create(PaymentCreateRequest req) {
        validateCreateRequest(req);

        if (req.getExternalId() != null) {
            payments.findByExternalId(req.getExternalId()).ifPresent(p -> {
                throw new ApiConflictException("DUPLICATE_EXTERNAL_ID", "Payment with this externalId already exists");
            });
        }

        Order order = orders.findById(req.getOrderId())
                .orElseThrow(() -> new ApiBadRequestException("ORDER_NOT_FOUND", "Order not found"));

        validateOrderForPayment(order, req.getAmount());

        Payment payment = createPayment(req, order);
        Payment saved = payments.save(payment);

        return paymentMapper.toDto(saved);
    }

    @Transactional
    public PaymentResponse updateStatus(Long id, PaymentStatus newStatus) {
        Payment payment = payments.findById(id)
                .orElseThrow(() -> new ApiBadRequestException("PAYMENT_NOT_FOUND", "Payment not found"));

        if (payment.getStatus() != newStatus) {
            payment.setStatus(newStatus);
            payment.setUpdatedAt(Instant.now());
            applyPaymentStatusEffects(payment);
        }

        return paymentMapper.toDto(payment);
    }

    public List<PaymentResponse> findByOrderId(Long orderId) {
        List<Payment> paymentList = payments.findByOrderId(orderId);
        return paymentMapper.toDtoList(paymentList);
    }

    private void validateCreateRequest(PaymentCreateRequest req) {
        if (req.getOrderId() == null || req.getOrderId() <= 0) {
            throw new ApiBadRequestException("BAD_ORDER_ID", "orderId must be > 0");
        }
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiBadRequestException("BAD_AMOUNT", "amount must be > 0");
        }
    }

    private void validateOrderForPayment(Order order, BigDecimal amount) {
        if (order.getStatus() != OrderStatus.NEW) {
            throw new ApiConflictException("ORDER_NOT_NEW", "Order must be NEW to create payment");
        }
        if (order.getTotal() == null) {
            throw new ApiConflictException("ORDER_TOTAL_MISSING", "Order has no total");
        }
        if (order.getTotal().compareTo(amount) != 0) {
            throw new ApiConflictException("AMOUNT_MISMATCH", "Amount doesn't match order total");
        }
    }

    private Payment createPayment(PaymentCreateRequest req, Order order) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(req.getAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setExternalId(req.getExternalId());
        payment.setUpdatedAt(Instant.now());
        return payment;
    }

    private void applyPaymentStatusEffects(Payment payment) {
        Order order = payment.getOrder();
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            if (order.getStatus() == OrderStatus.NEW) {
                orderService.changeStatus(order.getId(), OrderStatus.CONFIRMED);
            }
        } else if (payment.getStatus() == PaymentStatus.FAILED) {
            if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.CANCELLED) {
                orderService.changeStatus(order.getId(), OrderStatus.CANCELLED);
            }
        }
    }
}