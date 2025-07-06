package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.List;
import java.util.Optional;

@Component
public class MPAStorage extends BaseDbStorage<MPA> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa WHERE id = ?";

    public MPAStorage(JdbcTemplate jdbc, RowMapper<MPA> mapper) {
        super(jdbc, mapper, MPA.class);
    }

    public Optional<MPA> findById(Long mpaId) {
        return findOne(
                FIND_BY_ID_QUERY,
                mpaId
        );
    }

    public List<MPA> findAll() {
        return findMany(
                FIND_ALL_QUERY
        );
    }
}
