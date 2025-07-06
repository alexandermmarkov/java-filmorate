package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User user);

    User delete(Long userId);

    Optional<User> findById(Long userId);

    List<User> findAll();

    User addFriend(Long userId, Long friendId);

    User deleteFriend(Long userId, Long friendId);

    List<User> getFriends(User user);

    List<User> getCommonFriends(Long userId1, Long userId2);
}
