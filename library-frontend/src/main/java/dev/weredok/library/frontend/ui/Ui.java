package dev.weredok.library.frontend.ui;

import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.forms.CheckBox;
import org.dominokit.domino.ui.forms.IntegerBox;
import org.dominokit.domino.ui.forms.TextBox;

/**
 * Фабрика компонентів Domino UI. Сторінки створюють кнопки й поля тільки через цей клас —
 * якщо в іншій версії Domino UI зміниться API, правити доведеться лише тут.
 */
public final class Ui {

    private Ui() {
    }

    public static Button button(String text, Runnable onClick) {
        Button b = Button.create(text);
        b.element().classList.add("app-btn");
        b.addClickListener(evt -> onClick.run());
        return b;
    }

    public static Button primary(String text, Runnable onClick) {
        Button b = button(text, onClick);
        b.element().classList.add("app-btn-primary");
        return b;
    }

    public static Button danger(String text, Runnable onClick) {
        Button b = button(text, onClick);
        b.element().classList.add("app-btn-danger");
        return b;
    }

    public static Button small(String text, Runnable onClick) {
        Button b = button(text, onClick);
        b.element().classList.add("app-btn-small");
        return b;
    }

    public static void setEnabled(Button b, boolean enabled) {
        if (enabled) {
            b.element().removeAttribute("disabled");
            b.element().classList.remove("app-disabled");
        } else {
            b.element().setAttribute("disabled", "disabled");
            b.element().classList.add("app-disabled");
        }
    }

    public static TextBox textBox(String label) {
        TextBox t = TextBox.create(label);
        t.element().classList.add("app-field");
        return t;
    }

    /** Поле пароля: звичайний TextBox з type=password. */
    public static TextBox password(String label) {
        TextBox t = textBox(label);
        t.getInputElement().element().type = "password";
        return t;
    }

    public static IntegerBox intBox(String label) {
        IntegerBox i = IntegerBox.create(label);
        i.element().classList.add("app-field");
        return i;
    }

    public static CheckBox checkBox(String label, Runnable onChange) {
        CheckBox c = CheckBox.create(label);
        c.addChangeListener((oldValue, newValue) -> onChange.run());
        return c;
    }

    public static String value(TextBox t) {
        String v = t.getValue();
        return v == null ? "" : v;
    }

    public static boolean checked(CheckBox c) {
        return Boolean.TRUE.equals(c.getValue());
    }

    /** Виклик onInput на кожну зміну тексту (подія input спливає з внутрішнього &lt;input&gt;). */
    public static void onInput(TextBox t, Runnable onInput) {
        t.element().addEventListener("input", e -> onInput.run());
    }

    /** Enter у полі. */
    public static void onEnter(TextBox t, Runnable action) {
        t.element().addEventListener("keydown", e -> {
            elemental2.dom.KeyboardEvent ke = jsinterop.base.Js.uncheckedCast(e);
            if ("Enter".equals(ke.key)) {
                action.run();
            }
        });
    }
}
