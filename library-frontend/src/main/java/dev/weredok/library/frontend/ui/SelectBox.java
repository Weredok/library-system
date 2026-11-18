package dev.weredok.library.frontend.ui;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLOptionElement;
import elemental2.dom.HTMLSelectElement;
import jsinterop.base.Js;

/**
 * Простий випадаючий список на нативному &lt;select&gt;. Значення — рядки
 * (id категорії, назва enum тощо), порожній рядок = «не вибрано».
 */
public class SelectBox {

    private final HTMLElement root;
    private final HTMLSelectElement select;

    public SelectBox(String label) {
        root = Dom.div("app-select");
        if (label != null) {
            root.appendChild(Dom.text("label", "app-select-label", label));
        }
        select = Js.uncheckedCast(Dom.el("select", null));
        root.appendChild(select);
    }

    public SelectBox option(String value, String text) {
        HTMLOptionElement o = Js.uncheckedCast(Dom.el("option", null));
        o.value = value;
        o.textContent = text;
        select.appendChild(o);
        return this;
    }

    public void clearOptions() {
        Dom.clear(select);
    }

    public String getValue() {
        return select.value;
    }

    public void setValue(String value) {
        select.value = value == null ? "" : value;
    }

    public void onChange(Runnable r) {
        select.addEventListener("change", e -> r.run());
    }

    public HTMLElement element() {
        return root;
    }
}
