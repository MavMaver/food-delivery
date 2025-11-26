package dev.marievski.fooddelivery.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Репозиторий снимков профиля.
 */
public interface UserVersionRepository extends JpaRepository<UserVersion, Long> {

    // вернёт снимки пользователя до указанного момента, самый свежий — первым
    List<UserVersion> findByUserIdAndVersionAtLessThanEqualOrderByVersionAtDesc(Long userId, Instant at);
}
