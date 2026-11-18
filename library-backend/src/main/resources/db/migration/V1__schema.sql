-- Схема бібліотеки.
-- Принципи:
--  * історію видач ніколи не видаляємо: книги й читачі архівуються (archived_at), FK у loan — RESTRICT;
--  * кількість вільних примірників і статус видачі НЕ зберігаються, а рахуються з loan —
--    так вони завжди правдиві;
--  * обмеження в БД — останній рубіж після валідації на клієнті та сервері.

CREATE TABLE category (
    id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uq_category_name UNIQUE (name)
);

CREATE TABLE book (
    id           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    author       VARCHAR(255) NOT NULL,
    isbn         VARCHAR(13),
    year         INT,
    category_id  INT,
    total_copies INT NOT NULL DEFAULT 1,
    archived_at  TIMESTAMPTZ,
    CONSTRAINT uq_book_isbn UNIQUE (isbn),
    CONSTRAINT fk_book_category FOREIGN KEY (category_id) REFERENCES category (id) ON DELETE SET NULL,
    CONSTRAINT ck_book_total_copies CHECK (total_copies >= 0),
    CONSTRAINT ck_book_year CHECK (year IS NULL OR year BETWEEN 1400 AND 2100),
    CONSTRAINT ck_book_isbn_len CHECK (isbn IS NULL OR length(isbn) IN (10, 13))
);

CREATE TABLE reader (
    id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    full_name     VARCHAR(255) NOT NULL,
    ticket_number VARCHAR(50)  NOT NULL,
    phone         VARCHAR(50),
    email         VARCHAR(255),
    registered_at DATE NOT NULL DEFAULT CURRENT_DATE,
    archived_at   TIMESTAMPTZ,
    CONSTRAINT uq_reader_ticket UNIQUE (ticket_number),
    CONSTRAINT uq_reader_email UNIQUE (email)
);

CREATE TABLE app_user (
    id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username      VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_app_user_username UNIQUE (username),
    CONSTRAINT ck_app_user_role CHECK (role IN ('ADMIN', 'LIBRARIAN'))
);

-- Журнал видач. Один рядок = одна видача одного примірника.
CREATE TABLE loan (
    id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id       INT  NOT NULL,
    reader_id     INT  NOT NULL,
    issued_by     INT  NOT NULL,
    received_by   INT,
    issue_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date      DATE NOT NULL,
    return_date   DATE,
    renewal_count INT  NOT NULL DEFAULT 0,
    CONSTRAINT fk_loan_book FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE RESTRICT,
    CONSTRAINT fk_loan_reader FOREIGN KEY (reader_id) REFERENCES reader (id) ON DELETE RESTRICT,
    CONSTRAINT fk_loan_issued_by FOREIGN KEY (issued_by) REFERENCES app_user (id),
    CONSTRAINT fk_loan_received_by FOREIGN KEY (received_by) REFERENCES app_user (id),
    CONSTRAINT ck_loan_due CHECK (due_date >= issue_date),
    CONSTRAINT ck_loan_return CHECK (return_date IS NULL OR return_date >= issue_date),
    -- повернення фіксується разом з тим, хто прийняв книгу
    CONSTRAINT ck_loan_return_pair CHECK ((return_date IS NULL) = (received_by IS NULL)),
    CONSTRAINT ck_loan_renewals CHECK (renewal_count >= 0)
);

CREATE INDEX idx_loan_reader ON loan (reader_id);
CREATE INDEX idx_loan_book ON loan (book_id);
-- часткові індекси під найчастіші запити: «що зараз на руках» і «хто прострочив»
CREATE INDEX idx_loan_active_book ON loan (book_id) WHERE return_date IS NULL;
CREATE INDEX idx_loan_active_due ON loan (due_date) WHERE return_date IS NULL;

CREATE INDEX idx_book_category ON book (category_id);
