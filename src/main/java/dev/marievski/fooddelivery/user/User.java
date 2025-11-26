package dev.marievski.fooddelivery.user;

import dev.marievski.fooddelivery.common.Role;               // наша роль
import jakarta.persistence.*;                                 // JPA-аннотации
import jakarta.validation.constraints.Email;                  // валидация email
import jakarta.validation.constraints.NotBlank;               // валидация строк
import java.time.Instant;                                     // время создания

/**
 * Пользователь.
 * Принципы:
 *  - минимально нужные поля для тестового;
 *  - уникальный email (проверка и на БД, и в сервисе);
 *  - "мягкое удаление" через флаг active.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)       // автоинкремент (H2/PG)
    private Long id;

    @NotBlank                                                  // не пустое имя
    @Column(nullable = false)
    private String name;

    @Email                                                     // валидный формат
    @Column(nullable = false, unique = true)                   // уникальность на уровне БД
    private String email;

    @Enumerated(EnumType.STRING)                               // хранить роль текстом
    @Column(nullable = false)
    private Role role = Role.CUSTOMER;

    @Column(nullable = false)
    private boolean active = true;                             // деактивация вместо удаления

    @Column(nullable = false)
    private Instant createdAt = Instant.now();                 // когда создан

    // Геттеры/сеттеры. Без Lombok — на тестовом проще читать код и комментарии.
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
}
