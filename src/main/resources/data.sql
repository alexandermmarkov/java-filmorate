DELETE FROM films;
INSERT INTO films (name, description, release_date, duration)
VALUES
    ('Movie 1', 'description 1', '2016-07-20', 110),
    ('Movie 2', 'description 2', '2017-03-10', 130),
    ('Movie 3', 'description 3', '2021-11-05', 115);

DELETE FROM users;
INSERT INTO users (login, email, name, birthday)
VALUES
    ('user_1', 'user1@gmail.com', 'Uno', '1994-03-01'),
    ('user_2', 'user2@gmail.com', 'Dos', '1995-04-02'),
    ('user_3', 'user3@gmail.com', 'Tres', '1996-05-03');

DELETE FROM genres;
INSERT INTO genres (name)
VALUES
    ('Комедия'),
    ('Драма'),
    ('Мультфильм'),
    ('Триллер'),
    ('Документальный'),
    ('Боевик');

DELETE FROM mpa;
INSERT INTO mpa (id, name)
VALUES
    (1, 'G'),
    (2, 'PG'),
    (3, 'PG-13'),
    (4, 'R'),
    (5, 'NC-17');

DELETE FROM film_likes;
INSERT INTO film_likes (user_id, film_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 3)