package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.MPAStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MPAService {
    private final MPAStorage mpaStorage;

    public List<MPA> findAll() {
        List<MPA> fullList = mpaStorage.findAll();
        log.debug("the size of MPA map = {}", fullList);
        return fullList;
    }

    public MPA findById(Long mpaId) {
        Optional<MPA> mpa = mpaStorage.findById(mpaId);
        if (mpa.isEmpty()) {
            log.warn("no MPA with id = {}", mpaId);
            throw new NotFoundException("Рейтинг с id = " + mpaId + " не найден.");
        }
        return mpa.get();
    }
}
