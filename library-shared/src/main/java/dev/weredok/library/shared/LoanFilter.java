package dev.weredok.library.shared;

/**
 * Фільтр списку видач. ON_HAND включає і прострочені — це все, що зараз не повернуто.
 */
public enum LoanFilter {
    ALL("Усі"),
    ON_HAND("На руках"),
    OVERDUE("Прострочені"),
    RETURNED("Повернені");

    private final String label;

    LoanFilter(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
