package dev.weredok.library.frontend.ui;

/** Форматування для показу. */
public final class Fmt {

    private Fmt() {
    }

    /** 2026-11-18 → 18.11.2026 */
    public static String date(String iso) {
        if (iso == null || iso.length() < 10) {
            return "—";
        }
        return iso.substring(8, 10) + "." + iso.substring(5, 7) + "." + iso.substring(0, 4);
    }

    public static String orDash(Object value) {
        return value == null || "".equals(value) ? "—" : String.valueOf(value);
    }

    /** 1 день, 2 дні, 5 днів */
    public static String days(int n) {
        int mod10 = n % 10;
        int mod100 = n % 100;
        String word;
        if (mod10 == 1 && mod100 != 11) {
            word = "день";
        } else if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) {
            word = "дні";
        } else {
            word = "днів";
        }
        return n + " " + word;
    }
}
