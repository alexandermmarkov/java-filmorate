package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.List;
import java.util.Optional;

@Component
public class GenreStorage extends BaseDbStorage<Genre> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE id = ?";

    public GenreStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper, Genre.class);
    }

    public Optional<Genre> findById(Long genreId) {
        return findOne(
                FIND_BY_ID_QUERY,
                genreId
        );
    }

    public List<Genre> findAll() {
        return findMany(
                FIND_ALL_QUERY
        );
    }
}
