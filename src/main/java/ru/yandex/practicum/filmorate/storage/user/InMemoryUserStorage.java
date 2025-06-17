package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        User oldUser = users.get(user.getId());
        oldUser.setEmail(user.getEmail());
        if ((user.getLogin() != null) && !user.getLogin().isBlank())
            oldUser.setLogin(user.getLogin());
        if ((user.getName() != null) && !user.getName().isBlank()) {
            oldUser.setName(user.getName());
        } else if ((oldUser.getLogin() != null) && !oldUser.getLogin().isBlank()) {
            oldUser.setName(user.getLogin());
        }
        if ((user.getBirthday() != null) && !user.getBirthday().isAfter(LocalDate.now()))
            oldUser.setBirthday(user.getBirthday());
        return oldUser;
    }

    @Override
    public User delete(Long userId) {
        User user = users.get(userId);
        users.remove(userId);
        return user;
    }

    @Override
    public List<User> findAll() {
        return users.values().stream().toList();
    }

    @Override
    public Optional<User> findById(Long filmId) {
        return Optional.ofNullable(users.get(filmId));
    }

    public long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
