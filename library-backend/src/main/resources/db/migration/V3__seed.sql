-- Тестові дані для розробки. На продакшн-базі цю міграцію варто прибрати.
--
-- Користувачі: admin / admin (ADMIN), librarian / librarian (LIBRARIAN).
-- Паролі записані як {noop} — при першому вході Spring Security автоматично
-- перехешує їх у bcrypt (див. AppUserDetailsService.updatePassword).
-- ОБОВ'ЯЗКОВО змініть паролі, якщо база доступна не тільки вам.

INSERT INTO app_user (username, password_hash, role) VALUES
    ('admin', '{noop}admin', 'ADMIN'),
    ('librarian', '{noop}librarian', 'LIBRARIAN');

INSERT INTO category (name) VALUES
    ('Художня література'),
    ('Фантастика'),
    ('Програмування'),
    ('Математика'),
    ('Історія');

INSERT INTO book (title, author, isbn, year, category_id, total_copies) VALUES
    ('Чиста архітектура', 'Роберт Мартін', '9786171001008', 2019, (SELECT id FROM category WHERE name = 'Програмування'), 3),
    ('Java. Повне керівництво', 'Герберт Шилдт', '9786171001374', 2022, (SELECT id FROM category WHERE name = 'Програмування'), 2),
    ('Алгоритми', 'Томас Кормен', '9786171001749', 2020, (SELECT id FROM category WHERE name = 'Програмування'), 2),
    ('Дюна', 'Френк Герберт', '9786171002111', 2021, (SELECT id FROM category WHERE name = 'Фантастика'), 1),
    ('Тіні забутих предків', 'Михайло Коцюбинський', '9786171002487', 2018, (SELECT id FROM category WHERE name = 'Художня література'), 4),
    ('Лісова пісня', 'Леся Українка', '9786171002852', 2017, (SELECT id FROM category WHERE name = 'Художня література'), 2),
    ('Математичний аналіз', 'Григорій Фіхтенгольц', '9786171003224', 2015, (SELECT id FROM category WHERE name = 'Математика'), 3),
    ('Лінійна алгебра', 'Ілля Гельфанд', '9786171003590', 2016, (SELECT id FROM category WHERE name = 'Математика'), 2),
    ('Історія України', 'Наталя Яковенко', '9786171003965', 2019, (SELECT id FROM category WHERE name = 'Історія'), 2),
    ('Фундація', 'Айзек Азімов', '9786171004337', 2020, (SELECT id FROM category WHERE name = 'Фантастика'), 2),
    ('Кобзар', 'Тарас Шевченко', '9786171004702', 2014, (SELECT id FROM category WHERE name = 'Художня література'), 5),
    ('Без категорії: довідник', 'Колектив авторів', NULL, NULL, NULL, 1);

INSERT INTO reader (full_name, ticket_number, phone, email, registered_at) VALUES
    ('Іваненко Олена Петрівна', 'R-2026-000001', '+380501112233', 'olena@example.com', CURRENT_DATE - 120),
    ('Петренко Андрій Іванович', 'R-2026-000002', '+380672223344', 'andrii@example.com', CURRENT_DATE - 90),
    ('Коваль Марія Сергіївна', 'R-2026-000003', '+380933334455', NULL, CURRENT_DATE - 60),
    ('Шевчук Дмитро Олегович', 'R-2026-000004', NULL, 'dmytro@example.com', CURRENT_DATE - 30),
    ('Бондаренко Ірина Василівна', 'R-2026-000005', '+380504445566', 'iryna@example.com', CURRENT_DATE - 10);

SELECT setval('reader_ticket_seq', 5);

-- Видачі з датами відносно сьогодні, щоб прострочки були прострочками в будь-який день запуску
INSERT INTO loan (book_id, reader_id, issued_by, received_by, issue_date, due_date, return_date, renewal_count)
VALUES
    -- Олена: одна активна, одна прострочена
    ((SELECT id FROM book WHERE isbn = '9786171001008'), (SELECT id FROM reader WHERE ticket_number = 'R-2026-000001'),
     (SELECT id FROM app_user WHERE username = 'librarian'), NULL, CURRENT_DATE - 5, CURRENT_DATE + 9, NULL, 0),
    ((SELECT id FROM book WHERE isbn = '9786171001749'), (SELECT id FROM reader WHERE ticket_number = 'R-2026-000001'),
     (SELECT id FROM app_user WHERE username = 'librarian'), NULL, CURRENT_DATE - 20, CURRENT_DATE - 6, NULL, 0),
    -- Андрій: одна повернена, одна активна
    ((SELECT id FROM book WHERE isbn = '9786171001374'), (SELECT id FROM reader WHERE ticket_number = 'R-2026-000002'),
     (SELECT id FROM app_user WHERE username = 'admin'), (SELECT id FROM app_user WHERE username = 'librarian'),
     CURRENT_DATE - 30, CURRENT_DATE - 16, CURRENT_DATE - 18, 0),
    ((SELECT id FROM book WHERE isbn = '9786171001008'), (SELECT id FROM reader WHERE ticket_number = 'R-2026-000002'),
     (SELECT id FROM app_user WHERE username = 'librarian'), NULL, CURRENT_DATE - 2, CURRENT_DATE + 12, NULL, 0),
    -- Марія: активна, вже продовжена
    ((SELECT id FROM book WHERE isbn = '9786171002487'), (SELECT id FROM reader WHERE ticket_number = 'R-2026-000003'),
     (SELECT id FROM app_user WHERE username = 'librarian'), NULL, CURRENT_DATE - 24, CURRENT_DATE + 4, NULL, 1),
    -- Дмитро: прострочив єдиний примірник «Дюни»
    ((SELECT id FROM book WHERE isbn = '9786171002111'), (SELECT id FROM reader WHERE ticket_number = 'R-2026-000004'),
     (SELECT id FROM app_user WHERE username = 'librarian'), NULL, CURRENT_DATE - 16, CURRENT_DATE - 2, NULL, 0);
