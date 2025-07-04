package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {
    private final GenreStorage genreStorage;

    public List<Genre> findAll() {
        List<Genre> fullList = genreStorage.findAll();
        log.debug("the size of genre map = {}", fullList);
        return fullList;
    }

    public Genre findById(Long genreId) {
        Optional<Genre> genre = genreStorage.findById(genreId);
        if (genre.isEmpty()) {
            log.warn("no genre with id = {}", genreId);
            throw new NotFoundException("Жанр с id = " + genreId + " не найден.");
        }
        return genre.get();
    }
}
