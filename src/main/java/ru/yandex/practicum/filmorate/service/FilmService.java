package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film create(Film film) {
        if ((film.getReleaseDate() != null) && (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER,
                28)))) {
            log.error("film release date = {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        }
        if (film.getMpa() != null) {
            Optional<MPA> mpa = filmStorage.findMPAById(film.getMpa().getId());
            if (mpa.isEmpty()) {
                log.error("no MPA rating with id = {}", film.getMpa().getId());
                throw new NotFoundException("Рейтинг с id = " + film.getMpa().getId() + " не найден.");
            }
        }
        if (!film.getGenres().isEmpty()) {
            List<Long> unknownGenres = filmStorage.findUnknownFilmGenres(film.getGenres());
            if (!unknownGenres.isEmpty()) {
                log.error("no genres with id = {}", unknownGenres);
                throw new NotFoundException("Жанры с id = " + unknownGenres + " не найдены.");
            }
        }
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            log.error("no film id");
            throw new ValidationException("Id должен быть указан.");
        }
        if (filmStorage.findById(film.getId()).isPresent()) {
            if (film.getName() == null || film.getName().isBlank()) {
                log.error("film name = {}", film.getName());
                throw new ValidationException("Название не может быть пустым.");
            }
            return filmStorage.update(film);
        }
        log.error("no film with id = {}", film.getId());
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден.");
    }

    public List<Film> findAll() {
        List<Film> fullList = filmStorage.findAll();
        log.debug("the size of films map = {}", fullList);
        return fullList;
    }

    public Film findById(Long filmId) {
        Optional<Film> film = filmStorage.findById(filmId);
        if (film.isEmpty()) {
            log.error("no film with id = {}", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
        return film.get();
    }

    public Film delete(Long filmId) {
        if (filmStorage.findById(filmId).isEmpty()) {
            log.error("no film with id = {}", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
        return filmStorage.delete(filmId);
    }

    public Film likeAFilm(Long filmId, Long userId) {
        Optional<Film> film = filmStorage.findById(filmId);
        if (film.isEmpty()) {
            log.error("no film with id = {}", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
        User user = userService.findById(userId);
        return filmStorage.likeAFilm(filmId, userId).get();
    }

    public Film unlikeAFilm(Long filmId, Long userId) {
        Optional<Film> film = filmStorage.findById(filmId);
        if (film.isEmpty()) {
            log.error("no film with id = {}", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
        User user = userService.findById(userId);
        return filmStorage.unlikeAFilm(filmId, userId).get();
    }

    public List<Film> getTopFilms(int count) {
        log.debug("count = {}", count);
        return filmStorage.getTopFilms(count);
    }
}
