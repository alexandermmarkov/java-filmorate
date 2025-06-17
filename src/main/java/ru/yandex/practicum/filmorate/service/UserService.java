package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public User create(User user) {
        if (user.getLogin().contains(" ")) {
            log.warn("user login = {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы.");
        } else if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("user birthday = {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем.");
        } else if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("empty name, it equals login now - {}", user.getLogin());
        }

        user.setId(userStorage.getNextId());
        log.debug("user id = {}", user.getId());
        return userStorage.create(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            log.warn("no user id");
            throw new ValidationException("Id должен быть указан.");
        }
        if (userStorage.findById(user.getId()).isPresent()) {
            if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
                log.warn("user email = {}", user.getEmail());
                throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @.");
            }
            return userStorage.update(user);
        }
        log.warn("no user with id = {}", user.getId());
        throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден.");
    }

    public List<User> findAll() {
        List<User> fullList = userStorage.findAll();
        log.debug("the size of users map = {}", fullList);
        return fullList;
    }

    public User findById(Long userId) {
        Optional<User> user = userStorage.findById(userId);
        if (user.isEmpty()) {
            log.warn("no user with id = {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        return user.get();
    }

    public User delete(Long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            log.warn("no user with id = {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        return userStorage.delete(userId);
    }

    public User addFriend(Long userId, Long friendId) {
        if (userStorage.findById(userId).isEmpty()) {
            log.warn("no user with id = {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        if (userStorage.findById(friendId).isEmpty()) {
            log.warn("no user with id = {}", friendId);
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден.");
        }
        User user = userStorage.findById(userId).get();
        User friend = userStorage.findById(friendId).get();
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        return friend;
    }

    public User deleteFriend(Long userId, Long friendId) {
        if (userStorage.findById(userId).isEmpty()) {
            log.warn("no user with id = {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        if (userStorage.findById(friendId).isEmpty()) {
            log.warn("no user with id = {}", friendId);
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден.");
        }
        User user = userStorage.findById(userId).get();
        User friend = userStorage.findById(friendId).get();
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        return friend;
    }

    public List<User> getAllFriends(Long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            log.warn("no user with id = {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        User user = userStorage.findById(userId).get();
        return user.getFriends().stream()
                .filter(friendId -> userStorage.findById(friendId).isPresent())
                .map(friendId -> userStorage.findById(friendId).get())
                .toList();
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        if (userStorage.findById(userId).isEmpty()) {
            log.warn("no user with id = {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        if (userStorage.findById(otherId).isEmpty()) {
            log.warn("no user with id = {}", userId);
            throw new NotFoundException("Пользователь с id = " + otherId + " не найден.");
        }
        User user = userStorage.findById(userId).get();
        User other = userStorage.findById(otherId).get();
        return user.getFriends().stream()
                .filter(friendId -> other.getFriends().contains(friendId)
                        && userStorage.findById(friendId).isPresent())
                .map(friendId -> userStorage.findById(friendId).get())
                .toList();
    }
}
