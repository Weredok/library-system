package dev.weredok.library.shared;

import dev.weredok.library.shared.dto.BookFormDto;
import dev.weredok.library.shared.dto.ReaderFormDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Валідація форм. Той самий код виконується на клієнті (щоб одразу показати помилку)
 * і на сервері (бо клієнту довіряти не можна). Останній рубіж — обмеження в БД.
 * <p>
 * Методи normalize* змінюють форму на місці: обрізають пробіли, порожні рядки перетворюють на null.
 */
public final class Validation {

    private Validation() {
    }

    public static void normalizeBook(BookFormDto f) {
        f.setTitle(trimToNull(f.getTitle()));
        f.setAuthor(trimToNull(f.getAuthor()));
        f.setIsbn(Isbn.normalize(trimToNull(f.getIsbn())));
    }

    public static List<String> validateBook(BookFormDto f) {
        List<String> errors = new ArrayList<>();
        requireLength(errors, f.getTitle(), "Назва", 255);
        requireLength(errors, f.getAuthor(), "Автор", 255);
        if (f.getIsbn() != null && !Isbn.isValid(f.getIsbn())) {
            errors.add("ISBN некоректний (потрібно 10 або 13 цифр з правильною контрольною цифрою)");
        }
        if (f.getYear() != null
                && (f.getYear() < LibraryRules.MIN_BOOK_YEAR || f.getYear() > LibraryRules.MAX_BOOK_YEAR)) {
            errors.add("Рік має бути між " + LibraryRules.MIN_BOOK_YEAR + " і " + LibraryRules.MAX_BOOK_YEAR);
        }
        if (f.getTotalCopies() < 0) {
            errors.add("Кількість примірників не може бути від'ємною");
        }
        if (f.getTotalCopies() > 10000) {
            errors.add("Забагато примірників (максимум 10000)");
        }
        return errors;
    }

    public static void normalizeReader(ReaderFormDto f) {
        f.setFullName(collapseSpaces(trimToNull(f.getFullName())));
        f.setPhone(trimToNull(f.getPhone()));
        String email = trimToNull(f.getEmail());
        f.setEmail(email == null ? null : email.toLowerCase());
    }

    public static List<String> validateReader(ReaderFormDto f) {
        List<String> errors = new ArrayList<>();
        requireLength(errors, f.getFullName(), "ПІБ", 255);
        if (f.getPhone() != null && !isPhone(f.getPhone())) {
            errors.add("Телефон некоректний (цифри, пробіли, дужки, дефіси, можна + на початку; 7–15 цифр)");
        }
        if (f.getEmail() != null && !isEmail(f.getEmail())) {
            errors.add("Email некоректний");
        }
        if (f.getEmail() != null && f.getEmail().length() > 255) {
            errors.add("Email задовгий");
        }
        return errors;
    }

    public static List<String> validateCategoryName(String name) {
        List<String> errors = new ArrayList<>();
        requireLength(errors, trimToNull(name), "Назва категорії", 255);
        return errors;
    }

    // ---- helpers ----

    public static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String collapseSpaces(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean prevSpace = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean space = c == ' ' || c == '\t';
            if (space && prevSpace) {
                continue;
            }
            sb.append(space ? ' ' : c);
            prevSpace = space;
        }
        return sb.toString();
    }

    private static void requireLength(List<String> errors, String value, String field, int max) {
        if (value == null) {
            errors.add(field + ": обов'язкове поле");
        } else if (value.length() > max) {
            errors.add(field + ": не більше " + max + " символів");
        }
    }

    static boolean isPhone(String phone) {
        int digits = 0;
        for (int i = 0; i < phone.length(); i++) {
            char c = phone.charAt(i);
            if (c >= '0' && c <= '9') {
                digits++;
            } else if (c == '+') {
                if (i != 0) {
                    return false;
                }
            } else if (c != ' ' && c != '-' && c != '(' && c != ')') {
                return false;
            }
        }
        return digits >= 7 && digits <= 15;
    }

    static boolean isEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0 || at != email.lastIndexOf('@') || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1);
        int dot = domain.lastIndexOf('.');
        if (dot <= 0 || dot == domain.length() - 1) {
            return false;
        }
        for (int i = 0; i < email.length(); i++) {
            char c = email.charAt(i);
            if (c == ' ' || c == ',' || c == ';') {
                return false;
            }
        }
        return true;
    }
}
