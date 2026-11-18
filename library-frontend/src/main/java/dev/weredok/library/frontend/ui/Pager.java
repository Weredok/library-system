package dev.weredok.library.frontend.ui;

import elemental2.dom.HTMLElement;
import org.dominokit.domino.ui.button.Button;

import java.util.function.IntConsumer;

/** «← Сторінка 2 з 5 · всього 93 →». Сторінки рахуються з 0, показуються з 1. */
public class Pager {

    private final HTMLElement root = Dom.div("pager");
    private final HTMLElement info = Dom.span("");
    private final Button prev;
    private final Button next;
    private int page;
    private int pages;

    public Pager(IntConsumer goTo) {
        prev = Ui.small("←", () -> goTo.accept(page - 1));
        next = Ui.small("→", () -> goTo.accept(page + 1));
        Dom.append(root, prev.element(), info, next.element());
    }

    public void update(int page, int size, long total) {
        this.page = page;
        this.pages = (int) Math.max(1, (total + size - 1) / size);
        info.textContent = "Сторінка " + (page + 1) + " з " + pages + " · всього " + total;
        Ui.setEnabled(prev, page > 0);
        Ui.setEnabled(next, page + 1 < pages);
    }

    public HTMLElement element() {
        return root;
    }
}
