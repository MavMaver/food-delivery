package dev.marievski.fooddelivery.order;

/**
 * Статус заказа. Держим строки в БД (EnumType.STRING) для читаемости.
 * Минимально нужные статусы под тестовое и правила.
 */
public enum OrderStatus {
    NEW,         // только создан
    CONFIRMED,   // оплачен (по ТЗ после оплаты -> CONFIRMED)
    READY,       // готов к выдаче курьеру
    ASSIGNED,    // назначен курьер
    DELIVERING,  // в доставке
    DELIVERED,   // доставлен
    CANCELLED    // отменён
}
