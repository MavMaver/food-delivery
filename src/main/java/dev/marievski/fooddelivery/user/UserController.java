package dev.marievski.fooddelivery.user;

import dev.marievski.fooddelivery.common.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

/**
 * REST-слой. Контроллер тонкий: маппит DTO и зовёт сервис.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;
    public UserController(UserService service) { this.service = service; }

    /** DTO для создания. */
    public record UserCreateRq(@NotBlank String name,
                               @NotBlank @Email String email,
                               Role role) {}

    /** DTO для обновления. */
    public record UserUpdateRq(@NotBlank String name,
                               @NotBlank @Email String email,
                               Role role,
                               boolean active) {}

    /** DTO для ответа. */
    public record UserDto(Long id, String name, String email, Role role, boolean active) {
        public static UserDto of(User u) {
            return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.isActive());
        }
    }

    /** POST /users — регистрация пользователя. */
    @PostMapping
    public UserDto create(@Valid @RequestBody UserCreateRq rq) {
        User u = new User();
        u.setName(rq.name());
        u.setEmail(rq.email());
        u.setRole(rq.role() == null ? Role.CUSTOMER : rq.role());
        return UserDto.of(service.create(u));
    }

    /** GET /users/{id} — профиль пользователя. */
    @GetMapping("/{id}")
    public UserDto get(@PathVariable Long id) {
        return service.get(id)
                .map(UserDto::of)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

    /** PUT /users/{id} — обновление профиля. */
    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id, @Valid @RequestBody UserUpdateRq rq) {
        User patch = new User();
        patch.setName(rq.name());
        patch.setEmail(rq.email());
        patch.setRole(rq.role());
        patch.setActive(rq.active());
        try {
            return UserDto.of(service.update(id, patch));
        } catch (IllegalArgumentException e) {
            // 404 для отсутствующего пользователя, 409 для конфликта email — можно развести по сообщению
            if (e.getMessage() != null && e.getMessage().contains("не найден")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    /** GET /users — список с фильтром по роли + пагинация. */
    @GetMapping
    public Page<UserDto> list(@RequestParam(required = false) Role role,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
        return service.listByRole(role, PageRequest.of(page, size)).map(UserDto::of);
    }

    /** DELETE /users/{id} — деактивация (мягкое удаление). */
    @DeleteMapping("/{id}")
    public void deactivate(@PathVariable Long id) {
        try {
            service.deactivate(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * GET /users/{id}/versions/at?at=... — состояние профиля на дату.
     * Если снимок не найден — 404, а не 500.
     */
    @GetMapping("/{id}/versions/at")
    public Object snapshotAt(@PathVariable Long id, @RequestParam("at") Instant at) {
        return service.snapshotAt(id, at)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Снимок не найден"));
    }
}
