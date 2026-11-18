package dev.weredok.library.frontend.ui;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import org.dominokit.domino.ui.forms.TextBox;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Поле з автопошуком: вводиш текст → через 300 мс запит на сервер → список варіантів → клік обирає.
 *
 * @param <T> тип варіанту (ReaderDto, BookDto)
 */
public class Picker<T> {

    private final HTMLElement root = Dom.div("picker");
    private final TextBox input;
    private final HTMLElement results = Dom.div("picker-results");
    private final HTMLElement selectedView = Dom.div("picker-selected");
    private final BiConsumer<String, Consumer<List<T>>> search;
    private final Function<T, String> label;
    private final Function<T, String> hint;
    private Consumer<T> onSelect = t -> {
    };
    private double timer = -1;
    private T selected;

    /**
     * @param search виконує пошук за рядком і віддає результати в callback
     * @param label  основний текст варіанту
     * @param hint   дрібний текст під ним (може повертати null)
     */
    public Picker(String fieldLabel,
                  BiConsumer<String, Consumer<List<T>>> search,
                  Function<T, String> label,
                  Function<T, String> hint) {
        this.search = search;
        this.label = label;
        this.hint = hint;
        input = Ui.textBox(fieldLabel);
        Ui.onInput(input, this::schedule);
        Dom.append(root, input.element(), results, selectedView);
        results.style.display = "none";
    }

    public Picker<T> onSelect(Consumer<T> onSelect) {
        this.onSelect = onSelect;
        return this;
    }

    public T getSelected() {
        return selected;
    }

    public void select(T item) {
        selected = item;
        results.style.display = "none";
        Dom.clear(selectedView);
        if (item != null) {
            selectedView.appendChild(Dom.text("div", "picker-selected-label", "✓ " + label.apply(item)));
            String h = hint.apply(item);
            if (h != null) {
                selectedView.appendChild(Dom.text("div", "picker-hint", h));
            }
        }
        onSelect.accept(item);
    }

    /** Повторити пошук з поточним текстом (наприклад, після зміни фільтра). */
    public void refresh() {
        schedule();
    }

    private void schedule() {
        if (timer >= 0) {
            DomGlobal.clearTimeout(timer);
        }
        timer = DomGlobal.setTimeout(ignored -> run(), 300);
    }

    private void run() {
        timer = -1;
        String q = Ui.value(input).trim();
        if (q.isEmpty()) {
            results.style.display = "none";
            return;
        }
        search.accept(q, this::show);
    }

    private void show(List<T> items) {
        Dom.clear(results);
        if (items.isEmpty()) {
            results.appendChild(Dom.text("div", "picker-empty", "Нічого не знайдено"));
        }
        for (T item : items) {
            HTMLElement row = Dom.div("picker-item");
            row.appendChild(Dom.text("div", null, label.apply(item)));
            String h = hint.apply(item);
            if (h != null) {
                row.appendChild(Dom.text("div", "picker-hint", h));
            }
            row.addEventListener("click", e -> select(item));
            results.appendChild(row);
        }
        results.style.display = "block";
    }

    public HTMLElement element() {
        return root;
    }
}
