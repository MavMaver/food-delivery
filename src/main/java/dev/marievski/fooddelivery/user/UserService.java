package dev.marievski.fooddelivery.user;

import dev.marievski.fooddelivery.common.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Бизнес-логика вокруг пользователей.
 * SOLID: сервис отвечает только за пользователей и их версии.
 */
@Service
public class UserService {

    private final UserRepository users;
    private final UserVersionRepository versions;

    public UserService(UserRepository users, UserVersionRepository versions) {
        this.users = users;
        this.versions = versions;
    }

    /**
     * Создание пользователя с проверкой уникальности email.
     * Сразу после сохранения пишем ПЕРВИЧНЫЙ снимок в user_versions,
     * чтобы эндпоинт "состояние на дату" работал уже с момента создания.
     */
    @Transactional
    public User create(User u) {
        if (users.existsByEmail(u.getEmail())) {
            throw new IllegalArgumentException("Email уже используется");
        }
        User saved = users.save(u);
        // первичный снимок
        versions.save(new UserVersion(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getRole(),
                saved.isActive()
        ));
        return saved;
    }

    /** Получить по id. */
    public Optional<User> get(Long id) {
        return users.findById(id);
    }

    /** Пагинация + фильтр по роли (если роль не указана — вернуть всех). */
    public Page<User> listByRole(Role role, Pageable pageable) {
        return role == null ? users.findAll(pageable) : users.findByRole(role, pageable);
    }

    /**
     * Обновление: перед изменениями фиксируем предыдущую версию в user_versions.
     * Транзакция гарантирует атомарность снимка и изменений.
     */
    @Transactional
    public User update(Long id, User patch) {
        User u = users.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // снимок текущего состояния (до изменений)
        versions.save(new UserVersion(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.isActive()));

        if (patch.getName() != null) u.setName(patch.getName());
        if (patch.getEmail() != null) {
            if (!patch.getEmail().equals(u.getEmail()) && users.existsByEmail(patch.getEmail())) {
                throw new IllegalArgumentException("Email уже используется");
            }
            u.setEmail(patch.getEmail());
        }
        if (patch.getRole() != null) u.setRole(patch.getRole());
        u.setActive(patch.isActive());

        // возвращаем управляемую сущность — Hibernate сам сохранит изменения при commit
        return u;
    }

    /** Мягкое удаление — деактивация + снимок перед изменением. */
    @Transactional
    public void deactivate(Long id) {
        User u = users.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        versions.save(new UserVersion(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.isActive()));
        u.setActive(false);
    }

    /** Состояние профиля на указанное время (берём последний снимок <= at). */
    public Optional<UserVersion> snapshotAt(Long id, Instant at) {
        return versions.findByUserIdAndVersionAtLessThanEqualOrderByVersionAtDesc(id, at)
                .stream()
                .findFirst();
    }
}
