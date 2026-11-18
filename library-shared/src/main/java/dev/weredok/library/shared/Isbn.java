package dev.weredok.library.shared;

/**
 * Нормалізація і перевірка ISBN-10 / ISBN-13 з контрольною цифрою.
 * Без регулярних виразів, щоб однаково працювало і в JVM, і в GWT.
 */
public final class Isbn {

    private Isbn() {
    }

    /** Прибирає дефіси та пробіли, 'x' переводить у 'X'. null і порожній рядок дають null. */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '-' || c == ' ') {
                continue;
            }
            sb.append(c == 'x' ? 'X' : c);
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    /** Приймає вже нормалізований рядок. */
    public static boolean isValid(String isbn) {
        if (isbn == null) {
            return false;
        }
        if (isbn.length() == 13) {
            return isValid13(isbn);
        }
        if (isbn.length() == 10) {
            return isValid10(isbn);
        }
        return false;
    }

    private static boolean isValid13(String s) {
        int sum = 0;
        for (int i = 0; i < 13; i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
            int d = c - '0';
            sum += (i % 2 == 0) ? d : d * 3;
        }
        return sum % 10 == 0;
    }

    private static boolean isValid10(String s) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            char c = s.charAt(i);
            int d;
            if (c >= '0' && c <= '9') {
                d = c - '0';
            } else if (c == 'X' && i == 9) {
                d = 10;
            } else {
                return false;
            }
            sum += d * (10 - i);
        }
        return sum % 11 == 0;
    }
}
