package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Film delete(Long filmId);

    Optional<Film> findById(Long filmId);

    Optional<MPA> findMPAById(Long mpaId);

    Optional<Genre> findGenreById(Long genreId);

    List<Long> findUnknownFilmGenres(List<Genre> genres);

    List<Film> findAll();

    Optional<Film> likeAFilm(Long filmId, Long userId);

    Optional<Film> unlikeAFilm(Long filmId, Long userId);

    List<Film> getTopFilms(int count);
}
