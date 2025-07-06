package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.MPARowMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String FIND_MPA_BY_ID = "SELECT * FROM mpa WHERE id = ?";
    private static final String FIND_MPA_BY_FILM_ID = "SELECT * FROM mpa WHERE id " +
            "IN (SELECT mpa_id FROM films WHERE id = ?)";
    private static final String FIND_GENRE_BY_ID = "SELECT * FROM genres WHERE id = ?";
    private static final String FIND_GENRES_BY_FILM_ID = "SELECT * FROM genres WHERE id " +
            "IN (SELECT genre_id FROM film_genres WHERE film_id = ?)";
    private static final String LIKE_A_FILM = "INSERT INTO film_likes (film_id, user_id) " +
            "VALUES (?, ?)";
    private static final String UNLIKE_A_FILM = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_LIKES_BY_FILM_ID = "SELECT id FROM users WHERE id " +
            "IN (SELECT user_id FROM film_likes WHERE film_id = ?)";
    private static final String FIND_TOP_FILMS = "SELECT f.* FROM films f " +
            "LEFT OUTER JOIN film_likes fl ON f.id = fl.film_id " +
            "GROUP BY f.id " +
            "ORDER BY COUNT(f.id) DESC " +
            "LIMIT ?";
    private static final String INSERT_QUERY = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
            "duration = ?, mpa_id = ? WHERE id = ?";
    private static final String INSERT_GENRE_QUERY = "INSERT INTO film_genres SET film_id = ?, genre_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM films WHERE id = ?";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper, Film.class);
    }

    @Override
    public Film create(Film film) {
        Long lastId = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null
        );
        batchGenreUpdate(lastId, film.getGenres().stream().toList());
        film.setId(lastId);
        return film;
    }

    @Override
    public Film update(Film film) {
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId()
        );
        return film;
    }

    @Override
    public Film delete(Long filmId) {
        Film film = findById(filmId).get();
        delete(
                DELETE_QUERY,
                filmId
        );
        return film;
    }

    @Override
    public Optional<Film> findById(Long filmId) {
        Optional<Film> film = findOne(
                FIND_BY_ID_QUERY,
                filmId
        );
        film.ifPresent(this::getReferences);
        return film;
    }

    @Override
    public Optional<MPA> findMPAById(Long mpaId) {
        return queryForOptional(FIND_MPA_BY_ID, new MPARowMapper(), mpaId);
    }

    @Override
    public Optional<Genre> findGenreById(Long genreId) {
        return queryForOptional(FIND_GENRE_BY_ID, new GenreRowMapper(), genreId);
    }

    @Override
    public List<Long> findUnknownFilmGenres(List<Genre> genres) {
        if (genres.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = "SELECT id FROM genres";
        List<Long> genreIds = jdbc.queryForList(sql, Long.class);

        return new ArrayList<>(genres)
                .stream()
                .map(Genre::getId)
                .filter(genreId -> !genreIds.contains(genreId))
                .toList();
    }

    @Override
    public List<Film> findAll() {
        return findMany(
                FIND_ALL_QUERY
        );
    }

    @Override
    public Optional<Film> likeAFilm(Long filmId, Long userId) {
        insert(LIKE_A_FILM, filmId, userId);
        return findById(filmId);
    }

    @Override
    public Optional<Film> unlikeAFilm(Long filmId, Long userId) {
        update(UNLIKE_A_FILM, filmId, userId);
        return findById(filmId);
    }

    @Override
    public List<Film> getTopFilms(int count) {
        List<Film> topFilms = new ArrayList<>(findMany(FIND_TOP_FILMS, count));
        topFilms.forEach(this::getReferences);
        return topFilms;
    }

    private void getReferences(Film film) {
        queryForOptional(FIND_MPA_BY_FILM_ID, new MPARowMapper(), film.getId()).ifPresent(film::setMpa);
        film.getGenres().addAll(jdbc.query(FIND_GENRES_BY_FILM_ID, new GenreRowMapper(), film.getId()));
        film.getLikes().addAll(jdbc.queryForList(FIND_LIKES_BY_FILM_ID, Long.class, film.getId()));
    }

    private <T> Optional<T> queryForOptional(String sql, RowMapper<T> rowMapper, Object... args) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, rowMapper, args));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private void batchGenreUpdate(Long filmId, final List<Genre> genres) {
        List<Object[]> batch = new ArrayList<>();
        for (Genre genre : genres) {
            Object[] values = new Object[]{
                    filmId, genre.getId()
            };
            batch.add(values);
        }
        jdbc.batchUpdate(
                INSERT_GENRE_QUERY,
                batch
        );
    }
}
