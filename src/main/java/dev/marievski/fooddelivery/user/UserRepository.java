package dev.marievski.fooddelivery.user;

import dev.marievski.fooddelivery.common.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий пользователей. Spring Data сгенерирует реализацию.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);                 // проверка уникальности

    Page<User> findByRole(Role role, Pageable pageable); // фильтр по роли
}
