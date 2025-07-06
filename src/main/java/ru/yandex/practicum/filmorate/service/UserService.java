package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

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
        return userStorage.addFriend(userId, friendId);
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
        return userStorage.deleteFriend(userId, friendId);
    }

    public List<User> getAllFriends(Long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            log.warn("no user with id = {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        return userStorage.getFriends(userStorage.findById(userId).get());
    }

    public List<User> getCommonFriends(Long userId1, Long userId2) {
        Optional<User> optionalUser1 = userStorage.findById(userId1);
        if (optionalUser1.isEmpty()) {
            log.warn("no user with id = {}", userId1);
            throw new NotFoundException("Пользователь с id = " + userId1 + " не найден.");
        }

        Optional<User> optionalUser2 = userStorage.findById(userId2);
        if (optionalUser2.isEmpty()) {
            log.warn("no user with id = {}", userId2);
            throw new NotFoundException("Пользователь с id = " + userId2 + " не найден.");
        }
        return userStorage.getCommonFriends(userId1, userId2);
    }
}
