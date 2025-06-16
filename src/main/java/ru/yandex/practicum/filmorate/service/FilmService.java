package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public Film create(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
            log.error("film release date = {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        }

        film.setId(filmStorage.getNextId());
        log.debug("film id = {}", film.getId());
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
        if (filmStorage.findById(filmId).isEmpty()) {
            log.error("no film with id = {}", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
        Film film = filmStorage.findById(filmId).get();
        if (userService.findById(userId) != null) {
            film.getLikes().add(userId);
        }
        return film;
    }

    public Film unlikeAFilm(Long filmId, Long userId) {
        if (filmStorage.findById(filmId).isEmpty()) {
            log.error("no film with id = {}", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
        Film film = filmStorage.findById(filmId).get();
        if (userService.findById(userId) != null) {
            film.getLikes().remove(userId);
        }
        film.getLikes().remove(userId);
        return film;
    }

    public List<Film> getTopFilms(int count) {
        log.debug("count = {}", count);
        if (count <= 0) {
            throw new ValidationException("Некорректное значение параметра запроса count - должно быть " +
                    "положительным.");
        }
        return filmStorage.findAll().stream()
                .sorted((film1, film2) -> Integer.compare(film2.getLikes().size(), film1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
