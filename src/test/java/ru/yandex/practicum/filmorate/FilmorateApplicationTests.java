package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserRowMapper.class, UserDbStorage.class, FilmRowMapper.class, FilmDbStorage.class})
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    @Test
    public void testCreateUser() {
        Optional<User> newUser = Optional.ofNullable(
                userStorage.create(new User(null, "test@test.test", "testLogin", "testName",
                        LocalDate.now()))
        );

        assertThat(newUser)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 4L)
                );
    }

    @Test
    public void testUpdateUser() {
        Optional<User> userOptional = userStorage.findById(1L);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );

        User user = userOptional.get();
        user.setLogin("updatedLogin");
        User updatedUser = userStorage.update(user);

        assertThat(Optional.ofNullable(updatedUser))
                .isPresent()
                .hasValueSatisfying(userToUpdate ->
                        assertThat(userToUpdate).hasFieldOrPropertyWithValue("login", "updatedLogin")
                );
    }

    @Test
    public void testDeleteUser() {
        Optional<User> userOptional = userStorage.findById(1L);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );

        userStorage.delete(userOptional.get().getId());
        Optional<User> deletedUser = userStorage.findById(1L);

        assertThat(deletedUser).isEmpty();
    }

    @Test
    public void testFindUserById() {
        Optional<User> userOptional = userStorage.findById(1L);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void testFindAllUsers() {
        List<User> users = userStorage.findAll();

        assertThat(users).hasSize(3);
    }

    @Test
    public void testAddFriend() {
        User friend = userStorage.findById(2L).get();
        User user = userStorage.addFriend(1L, friend.getId());

        assertThat(user.getFriends()).hasSize(1);
        assertThat(user.getFriends()).contains(friend);
    }

    @Test
    public void testDeleteFriend() {
        User friend = userStorage.findById(2L).get();
        User user = userStorage.addFriend(1L, friend.getId());

        assertThat(user.getFriends()).contains(friend);

        user = userStorage.deleteFriend(user.getId(), friend.getId());

        assertThat(user.getFriends()).doesNotContain(friend);
    }

    @Test
    public void testGetFriends() {
        User user = userStorage.findById(1L).get();
        User friend1 = userStorage.findById(2L).get();
        user = userStorage.addFriend(user.getId(), friend1.getId());
        User friend2 = userStorage.findById(3L).get();
        user = userStorage.addFriend(user.getId(), friend2.getId());

        assertThat(user.getFriends())
                .hasSize(2)
                .contains(friend1, friend2);
    }

    @Test
    public void testGetCommonFriends() {
        User user1 = userStorage.findById(1L).get();

        userStorage.addFriend(2L, user1.getId());
        userStorage.addFriend(3L, user1.getId());
        List<User> commonFriends = userStorage.getCommonFriends(2L, 3L);

        assertThat(commonFriends).containsOnly(user1);
    }

    @Test
    public void testCreateFilm() {
        Optional<Film> newFilm = Optional.ofNullable(
                filmStorage.create(new Film(null, "Test Name", "Test Description", LocalDate.now(),
                        100, null))
        );

        assertThat(newFilm)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", 4L)
                );
    }

    @Test
    public void testUpdateFilm() {
        Optional<Film> filmOptional = filmStorage.findById(1L);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", 1L)
                );

        Film film = filmOptional.get();
        film.setName("Updated Name");
        Film updatedFilm = filmStorage.update(film);

        assertThat(Optional.ofNullable(updatedFilm))
                .isPresent()
                .hasValueSatisfying(filmToUpdate ->
                        assertThat(filmToUpdate).hasFieldOrPropertyWithValue("name", "Updated Name")
                );
    }

    @Test
    public void testDeleteFilm() {
        Optional<Film> filmOptional = filmStorage.findById(1L);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", 1L)
                );

        filmStorage.delete(filmOptional.get().getId());
        Optional<Film> deletedFilm = filmStorage.findById(1L);

        assertThat(deletedFilm).isEmpty();
    }

    @Test
    public void testFindFilmById() {
        Optional<Film> filmOptional = filmStorage.findById(1L);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void testFindMBAById() {
        Optional<MPA> mpaOptional = filmStorage.findMPAById(1L);

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void testFindGenreById() {
        Optional<Genre> mpaOptional = filmStorage.findGenreById(1L);

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(genre ->
                        assertThat(genre).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void testFindAllFilms() {
        List<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(3);
    }

    @Test
    public void testLikeAFilm() {
        Optional<Film> optionalFilm = filmStorage.likeAFilm(1L, 1L);

        assertThat(optionalFilm)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film.getLikes()).containsOnly(1L)
                );
    }

    @Test
    public void testUnlikeAFilm() {
        Optional<Film> optionalFilm = filmStorage.likeAFilm(1L, 1L);

        assertThat(optionalFilm)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film.getLikes()).containsOnly(1L)
                );

        optionalFilm = filmStorage.unlikeAFilm(1L, 1L);

        assertThat(optionalFilm)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film.getLikes()).isEmpty()
                );
    }

    @Test
    public void testGetTopFilms() {
        filmStorage.likeAFilm(1L, 2L);
        filmStorage.likeAFilm(1L, 3L);

        filmStorage.likeAFilm(3L, 1L);

        List<Film> topFilms = filmStorage.getTopFilms(2);
        assertThat(filmStorage.findById(1L).get().getLikes())
                .hasSize(3);
        assertThat(filmStorage.findById(3L).get().getLikes())
                .hasSize(2);
        assertThat(filmStorage.findById(2L).get().getLikes())
                .hasSize(1);
        assertThat(topFilms)
                .hasSize(2)
                .containsExactlyInAnyOrder(filmStorage.findById(1L).get(),
                        filmStorage.findById(3L).get());
    }
}
