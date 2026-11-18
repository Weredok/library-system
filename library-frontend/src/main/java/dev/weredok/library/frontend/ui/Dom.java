package dev.weredok.library.frontend.ui;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.Node;
import jsinterop.base.Js;

/** Мінімальні хелпери над elemental2 для розмітки сторінок. */
public final class Dom {

    private Dom() {
    }

    public static HTMLElement el(String tag, String cssClass) {
        HTMLElement e = Js.uncheckedCast(DomGlobal.document.createElement(tag));
        if (cssClass != null) {
            e.className = cssClass;
        }
        return e;
    }

    public static HTMLElement div(String cssClass) {
        return el("div", cssClass);
    }

    public static HTMLElement text(String tag, String cssClass, String text) {
        HTMLElement e = el(tag, cssClass);
        e.textContent = text == null ? "" : text;
        return e;
    }

    public static HTMLElement span(String text) {
        return text("span", null, text);
    }

    public static HTMLAnchorElement link(String text, String href) {
        HTMLAnchorElement a = Js.uncheckedCast(DomGlobal.document.createElement("a"));
        a.href = href;
        a.textContent = text;
        return a;
    }

    public static HTMLElement append(HTMLElement parent, Node... children) {
        for (Node c : children) {
            if (c != null) {
                parent.appendChild(c);
            }
        }
        return parent;
    }

    public static void clear(Element e) {
        while (e.firstChild != null) {
            e.removeChild(e.firstChild);
        }
    }

    /** Кольорова мітка: badge-ok, badge-warn, badge-danger, badge-muted. */
    public static HTMLElement badge(String text, String kind) {
        return text("span", "badge badge-" + kind, text);
    }
}
