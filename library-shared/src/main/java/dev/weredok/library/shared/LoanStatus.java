package dev.weredok.library.shared;

/**
 * Статус видачі. У базі не зберігається — обчислюється з return_date і due_date:
 * <ul>
 *     <li>RETURNED — return_date заповнена;</li>
 *     <li>OVERDUE — не повернена і due_date &lt; сьогодні;</li>
 *     <li>ACTIVE — не повернена і термін ще не минув.</li>
 * </ul>
 */
public enum LoanStatus {
    ACTIVE("На руках"),
    OVERDUE("Прострочено"),
    RETURNED("Повернено");

    private final String label;

    LoanStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
