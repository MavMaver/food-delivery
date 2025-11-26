package dev.marievski.fooddelivery.user;

import dev.marievski.fooddelivery.common.Role;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * Снимок профиля пользователя в момент изменения.
 * Простая реализация без Envers: пишем полную копию перед апдейтом.
 */
@Entity
@Table(name = "user_versions")
public class UserVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Сохраняем внешний ключ пользователя как простое Long-поле (легче запрашивать по userId+дате)
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private Instant versionAt = Instant.now(); // когда сделан снимок

    public UserVersion() {}

    // Удобный конструктор: перед апдейтом пробрасываем текущее состояние User
    public UserVersion(Long userId, String name, String email, Role role, boolean active) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
    public Instant getVersionAt() { return versionAt; }
}
