package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
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
        Optional<User> user = findById(userId);
        if (user.isEmpty()) {
            log.warn("no user with id = {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        users.remove(userId);
        return user.get();
    }

    @Override
    public List<User> findAll() {
        return users.values().stream().toList();
    }

    @Override
    public Optional<User> findById(Long filmId) {
        return Optional.ofNullable(users.get(filmId));
    }

    @Override
    public User addFriend(Long userId, Long friendId) {
        Optional<User> optionalUser = findById(userId);
        if (optionalUser.isEmpty()) {
            log.warn("no user with id = {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        User user = optionalUser.get();

        Optional<User> optionalFriend = findById(friendId);
        if (optionalFriend.isEmpty()) {
            log.warn("no user with id = {}", friendId);
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден.");
        }
        User friend = optionalFriend.get();
        user.getFriends().add(friend);
        return user;
    }

    @Override
    public User deleteFriend(Long userId, Long friendId) {
        Optional<User> optionalUser = findById(userId);
        if (optionalUser.isEmpty()) {
            log.warn("no user with id = {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        User user = optionalUser.get();

        Optional<User> optionalFriend = findById(friendId);
        if (optionalFriend.isEmpty()) {
            log.warn("no user with id = {}", friendId);
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден.");
        }
        User friend = optionalFriend.get();
        user.getFriends().remove(friend);
        return user;
    }

    @Override
    public List<User> getFriends(User user) {
        return user.getFriends().stream().toList();
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        User user = findById(userId1).get();
        User other = findById(userId2).get();
        List<User> getUserFriends = getFriends(user);
        List<User> getOtherUserFriends = getFriends(other);
        return getUserFriends.stream()
                .filter(getOtherUserFriends::contains)
                .toList();
    }

    @Override
    public Long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
