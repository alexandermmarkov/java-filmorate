package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();


    @Override
    public Film create(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        Film oldFilm = films.get(film.getId());
        oldFilm.setName(film.getName());
        if (film.getDescription() != null && !film.getDescription().isBlank())
            oldFilm.setDescription(film.getDescription());
        if (film.getDuration() >= 0)
            oldFilm.setDuration(film.getDuration());
        if (film.getReleaseDate() != null
                && !film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28)))
            oldFilm.setReleaseDate(film.getReleaseDate());
        return oldFilm;
    }

    @Override
    public Film delete(Long filmId) {
        Film film = films.get(filmId);
        films.remove(filmId);
        return film;
    }

    @Override
    public List<Film> findAll() {
        return films.values().stream().toList();
    }

    @Override
    public Optional<Film> findById(Long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    public long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
