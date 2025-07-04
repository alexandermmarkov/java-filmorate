package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("userDbStorage")
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_FRIENDS_BY_ID = "SELECT * FROM users WHERE id " +
            "IN (SELECT friend_id FROM friends WHERE user_id = ?)";
    private static final String FIND_FAVORITES_BY_ID = "SELECT * FROM films WHERE id " +
            "IN (SELECT film_id FROM film_likes WHERE user_id = ?)";
    private static final String INSERT_QUERY = "INSERT INTO users (email, login, name, birthday) " +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET login = ?, name = ?, birthday = ? " +
            "WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM users WHERE id = ?";
    private static final String ADD_FRIEND_SQL = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
    private static final String REMOVE_FRIEND_SQL = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String GET_FRIENDS_SQL = "SELECT * FROM users WHERE id " +
            "IN (SELECT friend_id FROM friends WHERE user_id = ?)";
    private static final String GET_COMMON_FRIENDS_SQL = "SELECT * FROM users u " +
            "JOIN friends f1 ON u.id = f1.friend_id " +
            "JOIN friends f2 ON u.id = f2.friend_id " +
            "WHERE f1.user_id = ? AND f2.user_id = ?";
    private Long lastId = 0L;

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper, User.class);
    }

    @Override
    public User create(User user) {
        lastId = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(lastId);
        return user;
    }

    @Override
    public User update(User user) {
        update(
                UPDATE_QUERY,
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public User delete(Long userId) {
        User user = findById(userId).get();
        delete(
                DELETE_QUERY,
                userId
        );
        return user;
    }

    @Override
    public Optional<User> findById(Long userId) {
        Optional<User> user = findOne(
                FIND_BY_ID_QUERY,
                userId
        );
        user.ifPresent(this::getReferences);
        return user;
    }

    @Override
    public List<User> findAll() {
        return findMany(
                FIND_ALL_QUERY
        );
    }

    @Override
    public User addFriend(Long userId, Long friendId) {
        update(ADD_FRIEND_SQL, userId, friendId);
        return findById(userId).get();
    }

    @Override
    public User deleteFriend(Long userId, Long friendId) {
        delete(REMOVE_FRIEND_SQL, userId, friendId);
        return findById(userId).get();
    }

    @Override
    public List<User> getFriends(User user) {
        return findMany(GET_FRIENDS_SQL, user.getId());
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        return findMany(GET_COMMON_FRIENDS_SQL, userId1, userId2);
    }

    @Override
    public Long getNextId() {
        return ++lastId;
    }

    protected void getReferences(User user) {
        if (user.getId() != null) {
            user.getFriends().addAll(jdbc.query(FIND_FRIENDS_BY_ID, new UserRowMapper(), user.getId()));
            user.getFavorites().addAll(jdbc.query(FIND_FAVORITES_BY_ID, new FilmRowMapper(), user.getId()));
        }
    }
}
