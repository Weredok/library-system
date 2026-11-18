package dev.weredok.library.shared;

/**
 * Бізнес-правила абонементу. Спільні для сервера (де вони реально перевіряються)
 * і клієнта (де ними тільки підказуємо користувачу).
 */
public final class LibraryRules {

    /** Скільки книг одночасно може бути на руках в одного читача. */
    public static final int MAX_ACTIVE_LOANS_PER_READER = 5;

    /** Термін видачі у днях. */
    public static final int LOAN_DAYS = 14;

    /** На скільки днів продовжується видача. */
    public static final int RENEWAL_DAYS = 14;

    /** Скільки разів можна продовжити одну видачу. */
    public static final int MAX_RENEWALS = 1;

    /** Максимальний розмір сторінки у списках. */
    public static final int MAX_PAGE_SIZE = 100;

    public static final int MIN_BOOK_YEAR = 1400;
    public static final int MAX_BOOK_YEAR = 2100;

    private LibraryRules() {
    }
}
