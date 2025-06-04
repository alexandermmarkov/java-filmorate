package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        // проверяем выполнение необходимых условий
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("user login = {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы.");
        } else if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.error("user birthday = {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем.");
        } else if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("empty name, it equals login now - {}", user.getLogin());
        }
        // формируем дополнительные данные
        user.setId(getNextId());
        log.debug("user id = {}", user.getId());
        // сохраняем нового пользователя в памяти приложения
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        // проверяем необходимые условия
        if (newUser.getId() == null) {
            log.error("no user id");
            throw new ValidationException("Id должен быть указан.");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (newUser.getEmail() == null || newUser.getEmail().isBlank() || !newUser.getEmail().contains("@")) {
                log.error("user email = {}", newUser.getEmail());
                throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @.");
            }
            // если пользователь найден и все условия соблюдены, обновляем его содержимое
            oldUser.setEmail(newUser.getEmail());
            if ((newUser.getLogin() != null) && !newUser.getLogin().isBlank())
                oldUser.setLogin(newUser.getLogin());
            if ((newUser.getName() != null) && !newUser.getName().isBlank()) {
                oldUser.setName(newUser.getName());
            } else if ((oldUser.getLogin() != null) && !oldUser.getLogin().isBlank()) {
                oldUser.setName(newUser.getLogin());
            }
            if ((newUser.getBirthday() != null) && !newUser.getBirthday().isAfter(LocalDate.now()))
                oldUser.setBirthday(newUser.getBirthday());
            return oldUser;
        }
        log.error("no film with id = {}", newUser.getId());
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден.");
    }

    // вспомогательный метод для генерации идентификатора нового фильма
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
