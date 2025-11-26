package dev.marievski.fooddelivery.common;

/**
 * Роль пользователя в системе.
 * Храню как строку (EnumType.STRING) — так миграции читаемее.
 */
public enum Role {
    CUSTOMER,   // клиент
    RESTAURANT, // представитель ресторана
    COURIER,    // курьер
    ADMIN       // администратор
}
