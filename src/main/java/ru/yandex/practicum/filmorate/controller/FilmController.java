package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Validated
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        return filmService.update(film);
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable @Positive Long id) {
        return filmService.findById(id);
    }

    @DeleteMapping("/{id}")
    public Film delete(@PathVariable @Positive Long id) {
        return filmService.delete(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film likeAFilm(@PathVariable @Positive Long id, @PathVariable @Positive Long userId) {
        return filmService.likeAFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film unlikeAFilm(@PathVariable @Positive Long id, @PathVariable @Positive Long userId) {
        return filmService.unlikeAFilm(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getTopFilms(@RequestParam(defaultValue = "10") @Positive int count) {
        return filmService.getTopFilms(count);
    }
}
