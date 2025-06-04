package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        // проверяем выполнение необходимых условий
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("film description length = {}", film.getDescription().length());
            throw new ValidationException("Максимальная длина описания — 200 символов.");
        } else if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1895,
                Month.DECEMBER, 28))) {
            log.error("film release date = {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        } else if (film.getDuration() < 0) {
            log.error("film duration = {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
        // формируем дополнительные данные
        film.setId(getNextId());
        log.debug("film id = {}", film.getId());
        // сохраняем новый фильм в памяти приложения
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        // проверяем необходимые условия
        if (newFilm.getId() == null) {
            log.error("no film id");
            throw new ValidationException("Id должен быть указан.");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getName() == null || newFilm.getName().isBlank()) {
                log.error("film name = {}", newFilm.getName());
                throw new ValidationException("Название не может быть пустым.");
            }
            // если фильм найден и все условия соблюдены, обновляем его описание
            oldFilm.setName(newFilm.getName());
            if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank())
                oldFilm.setDescription(newFilm.getDescription());
            if (newFilm.getDuration() >= 0)
                oldFilm.setDuration(newFilm.getDuration());
            if (newFilm.getReleaseDate() != null
                    && !newFilm.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28)))
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            return oldFilm;
        }
        log.error("no film with id = {}", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден.");
    }

    // вспомогательный метод для генерации идентификатора нового фильма
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
