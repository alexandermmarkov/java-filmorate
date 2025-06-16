package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

public class FilmTest {
    private Validator validator;
    String messageException;
    FilmController filmController = new FilmController(new FilmService(new InMemoryFilmStorage(),
            new UserService(new InMemoryUserStorage())));

    @BeforeEach
    public void beforeEach() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    public void readException(Film testFilm) {
        Set<ConstraintViolation<Film>> violations = validator.validate(testFilm);
        for (ConstraintViolation<Film> viol : violations) {
            messageException = viol.getMessage();
        }
    }

    @Test
    void addNewNormalFilm() {
        Film filmRequest = new Film(null, "Test film name", "Test film description",
                LocalDate.now().minusDays(1), 90);
        Film filmResponse = filmController.create(filmRequest);

        Assertions.assertEquals(filmRequest, filmResponse, "Фильм в запросе отличается от возвращаемого.");
    }

    @Test
    void addNewNullNameFilm() {
        Film testFilm = new Film(null, null, "Test film description",
                LocalDate.now().minusDays(1), 90);
        readException(testFilm);

        Assertions.assertEquals("Название фильма не может быть пустым", messageException,
                "Не выбрасывается исключение при добавлении фильма без названия.");
    }

    @Test
    void addNewBlankNameFilm() {
        Film testFilm = new Film(null, " ", "Test film description",
                LocalDate.now().minusDays(1), 90);
        readException(testFilm);

        Assertions.assertEquals("Название фильма не может быть пустым", messageException,
                "Не выбрасывается исключение при добавлении фильма с пустым названием.");
    }

    @Test
    void addNewNegativeDurationFilm() {
        Film testFilm = new Film(null, "Test film name", "Test film description",
                LocalDate.now().minusDays(1), -1);
        readException(testFilm);

        Assertions.assertEquals("Продолжительность фильма должна быть положительным числом", messageException,
                "Не выбрасывается исключение при добавлении фильма с отрицательной продолжительностью.");
    }

    @Test
    void addNewZeroDurationFilm() {
        Film filmRequest = new Film(null, "Test film name", "Test film description",
                LocalDate.now().minusDays(1), 0);
        Film filmResponse = filmController.create(filmRequest);

        Assertions.assertDoesNotThrow(() -> filmController.create(filmRequest),
                "Выбрасывается исключение при добавлении фильма с нулевой продолжительностью.");
        Assertions.assertTrue(filmRequest.equals(filmResponse) && (filmResponse.getDuration() == 0),
                "Фильм в запросе отличается от возвращаемого.");
    }

    @Test
    void addNew200SymbolsDescriptionFilm() {
        Film filmRequest = new Film(null,
                "Test name", "Test description of 200 symbols Test description of 200 symbols Test " +
                "description of 200 symbols Test description of 200 symbols Test description of 200 symbols Test " +
                "description of 200 symbols.........",
                LocalDate.now().minusDays(1), 90);

        Assertions.assertDoesNotThrow(() -> filmController.create(filmRequest),
                "Выбрасывается исключение при добавлении фильма с описанием в 200 символов.");
    }

    @Test
    void addNewTooLongDescriptionFilm() {
        Film testFilm = new Film(null,
                "Test name", "Test description that takes more than 200 symbols for the test to fail." +
                "Test description which takes more than 200 symbols for the test to fail. " +
                "Test description which takes more than 200 symbols for the test to fail.",
                LocalDate.now().minusDays(1), 90);
        readException(testFilm);

        Assertions.assertEquals("Максимальная длина описания — 200 символов", messageException,
                "Не выбрасывается исключение при добавлении фильма с отрицательной продолжительностью.");
    }

    @Test
    void addNewTooEarlyFilm() {
        Film filmRequest = new Film(null,
                "Test name", "Test description", LocalDate.of(1895, Month.DECEMBER, 27),
                90);

        Assertions.assertThrows(ValidationException.class, () -> filmController.create(filmRequest),
                "Не выбрасывается исключение при добавлении фильма с датой до 28 декабря 1895 года.");
    }

    @Test
    void addNew28Dec1895Film() {
        Film filmRequest = new Film(null,
                "Test name", "Test description", LocalDate.of(1895, Month.DECEMBER, 28),
                90);

        Assertions.assertDoesNotThrow(() -> filmController.create(filmRequest),
                "Выбрасывается исключение при добавлении фильма с датой 28 декабря 1895 года.");
    }

    @Test
    void updateNormalFilm() {
        Film filmRequest = new Film(null, "Test film name", "Test film description",
                LocalDate.now().minusDays(1), 90);
        Film filmResponse = filmController.create(filmRequest);
        Assertions.assertEquals(filmRequest, filmResponse, "Фильм в запросе отличается от возвращаемого.");

        filmResponse.setName("New name");
        filmResponse.setDescription("Test description of 200 symbols Test description of 200 symbols Test " +
                "description of 200 symbols Test description of 200 symbols Test description of 200 symbols Test " +
                "description of 200 symbols.........");
        filmResponse.setDuration(0);
        filmResponse.setReleaseDate(LocalDate.of(1895, Month.DECEMBER, 28));
        Film updatedFilm = filmController.update(filmResponse);

        Assertions.assertEquals(updatedFilm.getName(), filmResponse.getName(), "Фильм не был обновлён.");
    }

    @Test
    void updateNullNameFilm() {
        Film filmRequest = new Film(null, "Test film name", "Test film description",
                LocalDate.now().minusDays(1), 90);
        Film filmResponse = filmController.create(filmRequest);
        Assertions.assertEquals(filmRequest, filmResponse, "Фильм в запросе отличается от возвращаемого.");

        filmResponse.setName(null);

        Assertions.assertThrows(ValidationException.class, () -> filmController.update(filmResponse),
                "Не выбрасывается исключение при обновлении названия фильма на null.");
    }

    @Test
    void updateBlankNameFilm() {
        Film filmRequest = new Film(null, "Test film name", "Test film description",
                LocalDate.now().minusDays(1), 90);
        Film filmResponse = filmController.create(filmRequest);
        Assertions.assertEquals(filmRequest, filmResponse, "Фильм в запросе отличается от возвращаемого.");

        filmResponse.setName(" ");

        Assertions.assertThrows(ValidationException.class, () -> filmController.update(filmResponse),
                "Не выбрасывается исключение при обновлении названия фильма на пустое.");
    }

    @Test
    void updateNullDescriptionFilm() {
        Film filmRequest = new Film(null, "Test film name", "Test film description",
                LocalDate.now().minusDays(1), 90);
        Film filmResponse = filmController.create(filmRequest);
        Assertions.assertEquals(filmRequest, filmResponse, "Фильм в запросе отличается от возвращаемого.");

        filmResponse.setDescription(null);
        Film updatedFilm = filmController.update(filmResponse);

        Assertions.assertEquals(updatedFilm.getDescription(), filmRequest.getDescription(),
                "Описание фильма обновлено на null.");
    }

    @Test
    void updateBlankDescriptionFilm() {
        Film filmRequest = new Film(null, "Test film name", "Test film description",
                LocalDate.now().minusDays(1), 90);
        Film filmResponse = filmController.create(filmRequest);
        Assertions.assertEquals(filmRequest, filmResponse, "Фильм в запросе отличается от возвращаемого.");

        filmResponse.setDescription(" ");
        Film updatedFilm = filmController.update(filmResponse);

        Assertions.assertEquals(updatedFilm.getDescription(), filmRequest.getDescription(),
                "Описание фильма обновлено на пустое.");
    }

    @Test
    void updateNegativeDurationFilm() {
        Film filmRequest = new Film(null, "Test film name", "Test film description",
                LocalDate.now().minusDays(1), 90);
        Film filmResponse = filmController.create(filmRequest);
        Assertions.assertEquals(filmRequest, filmResponse, "Фильм в запросе отличается от возвращаемого.");

        filmResponse.setDuration(-1);
        Film updatedFilm = filmController.update(filmResponse);

        Assertions.assertEquals(updatedFilm.getReleaseDate(), filmRequest.getReleaseDate(),
                "Продолжительность фильма обновлена на отрицательное число минут.");
    }

    @Test
    void updateNullReleaseDateFilm() {
        Film filmRequest = new Film(null, "Test film name", "Test film description",
                LocalDate.now().minusDays(1), 90);
        Film filmResponse = filmController.create(filmRequest);
        Assertions.assertEquals(filmRequest, filmResponse, "Фильм в запросе отличается от возвращаемого.");

        filmResponse.setReleaseDate(null);
        Film updatedFilm = filmController.update(filmResponse);

        Assertions.assertEquals(updatedFilm.getReleaseDate(), filmRequest.getReleaseDate(),
                "Дата релиза фильма обновлена на null.");
    }

    @Test
    void updateTooEarlyReleaseDateFilm() {
        Film filmRequest = new Film(null, "Test film name", "Test film description",
                LocalDate.now().minusDays(1), 90);
        Film filmResponse = filmController.create(filmRequest);
        Assertions.assertEquals(filmRequest, filmResponse, "Фильм в запросе отличается от возвращаемого.");

        filmResponse.setReleaseDate(LocalDate.of(1895, Month.DECEMBER, 27));
        Film updatedFilm = filmController.update(filmResponse);

        Assertions.assertEquals(updatedFilm.getReleaseDate(), filmRequest.getReleaseDate(),
                "Дата релиза фильма обновлена на дату до 28 декабря 1895 года.");
    }

    @Test
    void updateNullIDFilm() {
        Film filmRequest = new Film(null, "Test film name", "Test film description",
                LocalDate.now().minusDays(1), 90);
        Film filmResponse = filmController.create(filmRequest);
        Assertions.assertEquals(filmRequest, filmResponse, "Фильм в запросе отличается от возвращаемого.");

        filmResponse.setId(null);

        Assertions.assertThrows(ValidationException.class, () -> filmController.update(filmResponse),
                "Не выбрасывается исключение при обновлении фильма с null ID.");
    }

    @Test
    void updateUknownIDFilm() {
        Film filmRequest = new Film(null, "Test film name", "Test film description",
                LocalDate.now().minusDays(1), 90);
        Film filmResponse = filmController.create(filmRequest);
        Assertions.assertEquals(filmRequest, filmResponse, "Фильм в запросе отличается от возвращаемого.");

        filmResponse.setId(filmResponse.getId() + 1);

        Assertions.assertThrows(NotFoundException.class, () -> filmController.update(filmResponse),
                "Не выбрасывается исключение при обновлении фильма с неизвестным ID.");
    }

    @Test
    void findAllFilms() {
        Film filmRequest1 = new Film(null, "Test film name 1", "Test film description 1",
                LocalDate.now().minusDays(2), 90);
        Film filmRequest2 = new Film(null, "Test film name 2", "Test film description 2",
                LocalDate.now().minusDays(1), 180);
        Film filmResponse1 = filmController.create(filmRequest1);
        Film filmResponse2 = filmController.create(filmRequest2);

        Assertions.assertEquals(filmRequest1, filmResponse1,
                "Первый фильм в запросе отличается от возвращаемого.");
        Assertions.assertEquals(filmRequest2, filmResponse2,
                "Второй фильм в запросе отличается от возвращаемого.");
        Assertions.assertTrue(filmController.findAll().size() == 2
                        && filmController.findAll().stream().toList().getFirst().equals(filmRequest1)
                        && filmController.findAll().stream().toList().getLast().equals(filmRequest2),
                "Получение всех фильмов работает некорректно.");
    }
}
