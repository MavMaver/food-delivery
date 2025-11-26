Food Delivery — тестовое задание

Мини-MVP сервиса доставки еды: пользователи, рестораны, меню с вариациями, корзина, заказы и платежи.
Стек: Java 17, Spring Boot 3, Spring MVC, JPA/Hibernate, Jakarta Validation, H2 (in-memory), Swagger UI, сценарии IntelliJ HTTP Client.

Быстрый старт (H2, ничего ставить не нужно)
Команда запуска:
mvn spring-boot:run

Полезные адреса:
Swagger UI: http://localhost:8080/swagger-ui/index.html

H2 Console: http://localhost:8080/h2-console

Доступ к H2:
JDBC URL: jdbc:h2:mem:fooddb
User: sa
Password: (пусто)

Конфигурация:
src/main/resources/application.yml (в проекте используется только H2; внешние СУБД и Docker не требуются).

Основные эндпоинты
POST /users — создать пользователя
POST /restaurants — создать ресторан
POST /restaurants/{id}/menu — добавить блюдо и вариации
POST /cart/items — добавить позицию в корзину
GET /cart?userId=... — получить корзину
POST /orders — создать заказ из корзины
GET /orders/{id} — получить заказ
PATCH /orders/{id}/status — сменить статус
DELETE /orders/{id} — отменить заказ
POST /payments — создать платёж (PENDING)
PATCH /payments/{id}/status — сменить статус платежа
GET /payments?orderId=... — платежи по заказу

Бизнес-правила (ошибки 409 CONFLICT)
• Минимальная сумма заказа: 300.00 — иначе 409.
• Недоступная вариация в корзине → 409 при создании заказа.
• Статусы заказа: NEW → CONFIRMED → READY → ASSIGNED → DELIVERING → DELIVERED (+ CANCELLED).
• DELETE /orders/{id} — отмена (конфликт, если уже DELIVERED).
• Платежи:
– POST /payments — идемпотентность по externalId (повтор → 409).
– PATCH /payments/{id}/status:
SUCCESS → заказ становится CONFIRMED (если был NEW);
FAILED → заказ становится CANCELLED (если ещё не DELIVERED/CANCELLED).

E2E-сценарий (IntelliJ HTTP Client)
Файл: food-delivery.http (в репозитории).
Запуск: открыть файл в IDEA и нажать ▶ Run all.
Сценарий: пользователь → ресторан/меню → корзина → заказ → смена статусов → платежи (SUCCESS/FAILED) + негативные кейсы 409.

Тесты
Юнит-тесты сервисов и несколько MockMvc.
Запуск тестов:
mvn test

Примечания
• Проект соответствует требованию «БД: H2 (in-memory)». Подключение к PostgreSQL и Docker не используются.
• H2 Console: при входе убедитесь, что JDBC URL точно jdbc:h2:mem:fooddb (User: sa, Password пустая).