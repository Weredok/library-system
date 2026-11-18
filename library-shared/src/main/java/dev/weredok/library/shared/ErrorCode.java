package dev.weredok.library.shared;

/**
 * Коди помилок, які повертає API. Повідомлення за замовчуванням — українською,
 * клієнт може показувати їх як є.
 */
public enum ErrorCode {
    VALIDATION_ERROR("Перевірте введені дані"),
    NOT_FOUND("Запис не знайдено"),
    UNAUTHORIZED("Потрібно увійти в систему"),
    BAD_CREDENTIALS("Невірний логін або пароль"),
    FORBIDDEN("Недостатньо прав для цієї дії"),

    ISBN_EXISTS("Книга з таким ISBN вже існує"),
    EMAIL_EXISTS("Читач з таким email вже зареєстрований"),
    TICKET_EXISTS("Читацький квиток з таким номером вже існує"),
    CATEGORY_EXISTS("Категорія з такою назвою вже існує"),

    BOOK_ARCHIVED("Книгу списано, з нею не можна працювати"),
    READER_ARCHIVED("Читача архівовано, видача неможлива"),
    BOOK_HAS_ACTIVE_LOANS("Книга зараз видана читачам — спочатку потрібно її повернути"),
    READER_HAS_ACTIVE_LOANS("У читача є неповернені книги"),
    TOTAL_BELOW_ON_LOAN("Кількість примірників не може бути меншою за кількість виданих"),

    NO_COPIES_AVAILABLE("Немає вільних примірників"),
    READER_HAS_OVERDUE("У читача є прострочені книги — нова видача заблокована"),
    LOAN_LIMIT_REACHED("Читач досяг ліміту книг на руках"),
    BOOK_ALREADY_ON_HAND("Цей читач вже має цю книгу на руках"),
    LOAN_ALREADY_RETURNED("Книгу вже повернено"),
    LOAN_OVERDUE_CANNOT_RENEW("Прострочену видачу не можна продовжити — спочатку поверніть книгу"),
    RENEWAL_LIMIT_REACHED("Видачу вже продовжували максимальну кількість разів"),

    DATA_CONFLICT("Конфлікт даних — запис змінився або порушує обмеження"),
    INTERNAL_ERROR("Внутрішня помилка сервера");

    private final String defaultMessage;

    ErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
