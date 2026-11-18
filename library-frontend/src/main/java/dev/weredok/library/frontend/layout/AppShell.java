package dev.weredok.library.frontend.layout;

import dev.weredok.library.frontend.Session;
import dev.weredok.library.frontend.ui.Dom;
import dev.weredok.library.frontend.ui.Ui;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLElement;

import java.util.LinkedHashMap;
import java.util.Map;

/** Шапка, бокове меню, область контенту. */
public class AppShell {

    private final HTMLElement root = Dom.div("shell");
    private final HTMLElement content = Dom.div("shell-content");
    private final Map<String, HTMLAnchorElement> links = new LinkedHashMap<>();

    public AppShell(Runnable onLogout) {
        HTMLElement header = Dom.div("shell-header");
        header.appendChild(Dom.text("div", "shell-title", "📚 Бібліотека"));
        HTMLElement user = Dom.div("shell-user");
        String role = Session.isAdmin() ? "адміністратор" : "бібліотекар";
        user.appendChild(Dom.text("span", "shell-username",
                Session.user().getUsername() + " · " + role));
        user.appendChild(Ui.small("Вийти", onLogout).element());
        header.appendChild(user);

        HTMLElement nav = Dom.el("nav", "shell-nav");
        addLink(nav, "loans", "Абонемент");
        addLink(nav, "overdue", "Боржники");
        addLink(nav, "books", "Книги");
        addLink(nav, "readers", "Читачі");

        HTMLElement body = Dom.div("shell-body");
        Dom.append(body, nav, content);
        Dom.append(root, header, body);
    }

    private void addLink(HTMLElement nav, String section, String text) {
        HTMLAnchorElement a = Dom.link(text, "#" + section);
        a.className = "shell-link";
        links.put(section, a);
        nav.appendChild(a);
    }

    public void show(String section, HTMLElement page) {
        links.forEach((key, a) -> {
            if (key.equals(section)) {
                a.classList.add("active");
            } else {
                a.classList.remove("active");
            }
        });
        Dom.clear(content);
        content.appendChild(page);
    }

    public HTMLElement element() {
        return root;
    }
}
