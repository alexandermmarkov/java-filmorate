package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private final Map<Long, MPA> mpa = new HashMap<>();
    private final Map<Long, Genre> genres = new HashMap<>();
    private UserStorage userStorage;


    @Override
    public Film create(Film film) {
        film.setId(getNextId());
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

    @Override
    public Optional<MPA> findMPAById(Long mpaId) {
        return Optional.ofNullable(mpa.get(mpaId));
    }

    @Override
    public Optional<Genre> findGenreById(Long genreId) {
        return Optional.ofNullable(genres.get(genreId));
    }

    @Override
    public Optional<Film> likeAFilm(Long filmId, Long userId) {
        Optional<Film> film = findById(filmId);
        Optional<User> user = userStorage.findById(userId);
        if (film.isPresent() && user.isPresent()) {
            film.get().getLikes().add(userId);
            return film;
        }
        return Optional.empty();
    }

    @Override
    public Optional<Film> unlikeAFilm(Long userId, Long filmId) {
        Optional<Film> film = findById(filmId);
        Optional<User> user = userStorage.findById(userId);
        if (film.isPresent() && user.isPresent()) {
            film.get().getLikes().remove(userId);
            return film;
        }
        return Optional.empty();
    }

    @Override
    public List<Film> getTopFilms(int count) {
        return findAll().stream()
                .sorted((film1, film2) -> Integer.compare(film2.getLikes().size(), film1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public Long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
