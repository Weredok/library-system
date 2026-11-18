package dev.weredok.library.frontend.ui;

import elemental2.dom.HTMLElement;
import elemental2.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Проста таблиця: колонки задаються функціями «рядок → вміст комірки».
 * Свідомо не DataTable з Domino UI — так менше залежності від його API,
 * а пагінація, пошук і фільтри все одно серверні.
 */
public class Table<T> {

    private static final class Column<T> {
        final String header;
        final Function<T, Node> cell;
        final String cssClass;

        Column(String header, Function<T, Node> cell, String cssClass) {
            this.header = header;
            this.cell = cell;
            this.cssClass = cssClass;
        }
    }

    private final List<Column<T>> columns = new ArrayList<>();
    private final HTMLElement root = Dom.div("table-wrap");
    private final HTMLElement table = Dom.el("table", "app-table");
    private final HTMLElement thead = Dom.el("thead", null);
    private final HTMLElement tbody = Dom.el("tbody", null);
    private Function<T, String> rowClass = t -> null;

    public Table() {
        table.appendChild(thead);
        table.appendChild(tbody);
        root.appendChild(table);
    }

    public Table<T> text(String header, Function<T, String> value) {
        return column(header, t -> Dom.span(value.apply(t)), null);
    }

    public Table<T> column(String header, Function<T, Node> cell) {
        return column(header, cell, null);
    }

    public Table<T> column(String header, Function<T, Node> cell, String cssClass) {
        columns.add(new Column<>(header, cell, cssClass));
        return this;
    }

    public Table<T> rowClass(Function<T, String> rowClass) {
        this.rowClass = rowClass;
        return this;
    }

    public void setItems(List<T> items, String emptyText) {
        Dom.clear(thead);
        HTMLElement hr = Dom.el("tr", null);
        for (Column<T> c : columns) {
            hr.appendChild(Dom.text("th", c.cssClass, c.header));
        }
        thead.appendChild(hr);

        Dom.clear(tbody);
        if (items == null || items.isEmpty()) {
            HTMLElement tr = Dom.el("tr", null);
            HTMLElement td = Dom.text("td", "table-empty", emptyText);
            td.setAttribute("colspan", String.valueOf(columns.size()));
            tr.appendChild(td);
            tbody.appendChild(tr);
            return;
        }
        for (T item : items) {
            HTMLElement tr = Dom.el("tr", rowClass.apply(item));
            for (Column<T> c : columns) {
                HTMLElement td = Dom.el("td", c.cssClass);
                Node content = c.cell.apply(item);
                if (content != null) {
                    td.appendChild(content);
                }
                tr.appendChild(td);
            }
            tbody.appendChild(tr);
        }
    }

    public HTMLElement element() {
        return root;
    }
}
