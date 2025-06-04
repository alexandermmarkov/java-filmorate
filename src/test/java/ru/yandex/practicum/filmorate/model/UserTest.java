package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

public class UserTest {
    private final UserController userController = new UserController();

    @Test
    void addNewNormalUser() {
        User userRequest = new User(null, "user@email.com", "testUserLogin", "Test user name",
                LocalDate.now());
        User userResponse = userController.create(userRequest);
        userRequest.setId(userResponse.getId());

        Assertions.assertEquals(userRequest, userResponse,
                "Пользователь в запросе отличается от возвращаемого.");
    }

    @Test
    void addNewNullEmailUser() {
        User userRequest = new User(null, null, "testUserLogin", "Test user name",
                LocalDate.now().minusYears(18));

        Assertions.assertThrows(ValidationException.class, () -> userController.create(userRequest),
                "Не выбрасывается исключение при добавлении пользователя без почты.");
    }

    @Test
    void addNewBlankEmailUser() {
        User userRequest = new User(null, " ", "testUserLogin", "Test user name",
                LocalDate.now().minusYears(18));

        Assertions.assertThrows(ValidationException.class, () -> userController.create(userRequest),
                "Не выбрасывается исключение при добавлении пользователя с пустой почтой.");
    }

    @Test
    void addNewInvalidEmailUser() {
        User userRequest = new User(null, "это-неправильный?эмейл@", "testUserLogin",
                "Test user name", LocalDate.now().minusYears(18));

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Assertions.assertTrue(factory.getValidator().validate(userRequest).size() == 1
                    && factory.getValidator().validate(userRequest).stream().toList().getFirst().getPropertyPath()
                            .toString().equals("email"),
                    "Не выбрасывается исключение при добавлении пользователя с некорректной почтой.");
        }
    }

    @Test
    void addNewNullLoginUser() {
        User userRequest = new User(null, "user@mail.com", null, "Test user name",
                LocalDate.now().minusYears(18));

        Assertions.assertThrows(ValidationException.class, () -> userController.create(userRequest),
                "Не выбрасывается исключение при добавлении пользователя без логина.");
    }

    @Test
    void addNewEmptyLoginUser() {
        User userRequest = new User(null, "user@mail.com", "", "Test user name",
                LocalDate.now().minusYears(18));

        Assertions.assertThrows(ValidationException.class, () -> userController.create(userRequest),
                "Не выбрасывается исключение при добавлении пользователя с пустым логином.");
    }

    @Test
    void addNewLoginWithSpacesUser() {
        User userRequest = new User(null, "user@mail.com", "Test login", "Test user name",
                LocalDate.now().minusYears(18));

        Assertions.assertThrows(ValidationException.class, () -> userController.create(userRequest),
                "Не выбрасывается исключение при добавлении пользователя с логином с пробелами.");
    }

    @Test
    void addNewNullNameUser() {
        User userRequest = new User(null, "user@mail.com", "testUserLogin", null,
                LocalDate.now().minusYears(18));
        User userResponse = userController.create(userRequest);

        Assertions.assertEquals(userRequest.getLogin(), userResponse.getName(),
                "При отсутствии имени оно не заполняется логином.");
    }

    @Test
    void addNewBlankNameUser() {
        User userRequest = new User(null, "user@mail.com", "testUserLogin", "",
                LocalDate.now().minusYears(18));
        User userResponse = userController.create(userRequest);

        Assertions.assertEquals(userRequest.getLogin(), userResponse.getName(),
                "При пустом имени оно не заполняется логином.");
    }

    @Test
    void addNewTodayBirthdayUser() {
        User userRequest = new User(null, "user@mail.com", "testUserLogin", "Test user name",
                LocalDate.now());

        Assertions.assertDoesNotThrow(() -> userController.create(userRequest),
                "При дне рождения сегодня выбрасывается исключение.");
    }

    @Test
    void addNewFutureBirthdayUser() {
        User userRequest = new User(null, "user@mail.com", "testUserLogin", "Test user name",
                LocalDate.now().plusDays(1));

        Assertions.assertThrows(ValidationException.class, () -> userController.create(userRequest),
                "Не выбрасывается исключение при добавлении пользователя с датой рождения в будущем.");
    }

    @Test
    void updateNormalUser() {
        User userRequest = new User(null, "user@email.com", "testUserLogin", "Test user name",
                LocalDate.now().minusDays(1));
        User userResponse = userController.create(userRequest);

        Assertions.assertEquals(userRequest, userResponse,
                "Пользователь в запросе отличается от возвращаемого.");

        userResponse.setEmail("new-" + userResponse.getEmail());
        userResponse.setLogin("new-" + userResponse.getLogin());
        userResponse.setName("new-" + userResponse.getName());
        userResponse.setBirthday(LocalDate.now());
        User updatedUser = userController.update(userResponse);

        Assertions.assertTrue(updatedUser.getEmail().equals(userResponse.getEmail())
                && updatedUser.getLogin().equals(userResponse.getLogin())
                && updatedUser.getName().equals(userResponse.getName())
                && updatedUser.getBirthday().isEqual(userResponse.getBirthday()),
                "Пользователь не был обновлён.");
    }

    @Test
    void updateNullEmailUser() {
        User userRequest = new User(null, "user@email.com", "testUserLogin", "Test user name",
                LocalDate.now());
        User userResponse = userController.create(userRequest);

        Assertions.assertEquals(userRequest, userResponse,
                "Пользователь в запросе отличается от возвращаемого.");

        userResponse.setEmail(null);

        Assertions.assertThrows(ValidationException.class, () -> userController.update(userResponse),
                "Не выбрасывается исключение при обновлении почты пользователя на null.");
    }

    @Test
    void updateBlankEmailUser() {
        User userRequest = new User(null, "user@email.com", "testUserLogin", "Test user name",
                LocalDate.now());
        User userResponse = userController.create(userRequest);

        Assertions.assertEquals(userRequest, userResponse,
                "Пользователь в запросе отличается от возвращаемого.");

        userResponse.setEmail(" ");

        Assertions.assertThrows(ValidationException.class, () -> userController.update(userResponse),
                "Не выбрасывается исключение при обновлении почты пользователя на пустую.");
    }

    @Test
    void updateNullLoginUser() {
        User userRequest = new User(null, "user@email.com", "testUserLogin", "Test user name",
                LocalDate.now());
        User userResponse = userController.create(userRequest);

        Assertions.assertEquals(userRequest, userResponse,
                "Пользователь в запросе отличается от возвращаемого.");

        userResponse.setLogin(null);
        User updatedUser = userController.update(userResponse);

        Assertions.assertEquals(updatedUser.getLogin(), userRequest.getLogin(),
                "Логин пользователя обновлён на null.");
    }

    @Test
    void updateBlankLoginUser() {
        User userRequest = new User(null, "user@email.com", "testUserLogin", "Test user name",
                LocalDate.now());
        User userResponse = userController.create(userRequest);

        Assertions.assertEquals(userRequest, userResponse, "Пользователь в запросе отличается от возвращаемого.");

        userResponse.setLogin(" ");
        User updatedUser = userController.update(userResponse);

        Assertions.assertEquals(updatedUser.getLogin(), userRequest.getLogin(),
                "Логин пользователя обновлён на пустой.");
    }

    @Test
    void updateNullNameUser() {
        User userRequest = new User(null, "user@email.com", "testUserLogin", "Test user name",
                LocalDate.now());
        User userResponse = userController.create(userRequest);

        Assertions.assertEquals(userRequest, userResponse,
                "Пользователь в запросе отличается от возвращаемого.");

        userResponse.setName(null);
        User updatedUser = userController.update(userResponse);

        Assertions.assertEquals(updatedUser.getName(), userRequest.getName(),
                "Имя пользователя обновлено на null.");
    }

    @Test
    void updateBlankNameUser() {
        User userRequest = new User(null, "user@email.com", "testUserLogin", "Test user name",
                LocalDate.now());
        User userResponse = userController.create(userRequest);

        Assertions.assertEquals(userRequest, userResponse,
                "Пользователь в запросе отличается от возвращаемого.");

        userResponse.setName(" ");
        User updatedUser = userController.update(userResponse);

        Assertions.assertEquals(updatedUser.getName(), userRequest.getName(),
                "Имя пользователя обновлено на пустое.");
    }

    @Test
    void updateFutureBirthdayUser() {
        User userRequest = new User(null, "user@email.com", "testUserLogin", "Test user name",
                LocalDate.now());
        User userResponse = userController.create(userRequest);

        Assertions.assertEquals(userRequest, userResponse,
                "Пользователь в запросе отличается от возвращаемого.");

        userResponse.setBirthday(LocalDate.now().plusDays(1));
        User updatedUser = userController.update(userResponse);

        Assertions.assertEquals(updatedUser.getBirthday(), userRequest.getBirthday(),
                "День рождения пользователя изменён на день в будущем.");
    }

    @Test
    void updateNullIDFilm() {
        User userRequest = new User(null, "user@email.com", "testUserLogin", "Test user name",
                LocalDate.now());
        User userResponse = userController.create(userRequest);

        Assertions.assertEquals(userRequest, userResponse,
                "Пользователь в запросе отличается от возвращаемого.");

        userResponse.setId(null);

        Assertions.assertThrows(ValidationException.class, () -> userController.update(userResponse),
                "Не выбрасывается исключение при обновлении пользователя с неизвестным ID.");
    }

    @Test
    void updateUknownIDFilm() {
        User userRequest = new User(null, "user@email.com", "testUserLogin", "Test user name",
                LocalDate.now());
        User userResponse = userController.create(userRequest);

        Assertions.assertEquals(userRequest, userResponse,
                "Пользователь в запросе отличается от возвращаемого.");

        userResponse.setId(userResponse.getId() + 1);

        Assertions.assertThrows(NotFoundException.class, () -> userController.update(userResponse),
                "Не выбрасывается исключение при обновлении пользователя с неизвестным ID.");
    }

    @Test
    void findAllFilms() {
        User userRequest1 = new User(null, "user1@email.com", "testUserLogin1", "Test user1 name",
                LocalDate.now());
        User userRequest2 = new User(null, "user2@email.com", "testUserLogin2", "Test user2 name",
                LocalDate.now());
        User userResponse1 = userController.create(userRequest1);
        User userResponse2 = userController.create(userRequest2);
        Assertions.assertEquals(userRequest1, userResponse1,
                "Первый пользователь в запросе отличается от возвращаемого.");
        Assertions.assertEquals(userRequest2, userResponse2,
                "Второй пользователь в запросе отличается от возвращаемого.");
        Assertions.assertTrue(userController.findAll().size() == 2
                        && userController.findAll().stream().toList().getFirst().equals(userRequest1)
                        && userController.findAll().stream().toList().getLast().equals(userRequest2),
                "Получение всех пользователей работает некорректно.");
    }
}
