package dev.weredok.library.frontend;

import dev.weredok.library.frontend.layout.AppShell;
import dev.weredok.library.frontend.pages.BooksPage;
import dev.weredok.library.frontend.pages.LoansPage;
import dev.weredok.library.frontend.pages.OverduePage;
import dev.weredok.library.frontend.pages.ReaderDetailsPage;
import dev.weredok.library.frontend.pages.ReadersPage;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;

/**
 * Маршрути через hash: #books, #readers, #reader/12, #loans, #overdue.
 * Кнопка «назад» у браузері працює сама.
 */
public final class Router {

    private final AppShell shell;

    public Router(AppShell shell) {
        this.shell = shell;
        DomGlobal.window.addEventListener("hashchange", e -> route());
    }

    public static void go(String hash) {
        DomGlobal.location.hash = hash;
    }

    public void route() {
        String hash = DomGlobal.location.hash;
        String path = hash == null || hash.length() <= 1 ? "loans" : hash.substring(1);
        String[] parts = path.split("/");
        String section = parts[0];

        HTMLElement page;
        switch (section) {
            case "books" -> page = new BooksPage().element();
            case "readers" -> page = new ReadersPage().element();
            case "reader" -> {
                int id = parts.length > 1 ? parseId(parts[1]) : 0;
                if (id <= 0) {
                    go("readers");
                    return;
                }
                page = new ReaderDetailsPage(id).element();
                section = "readers";
            }
            case "overdue" -> page = new OverduePage().element();
            default -> {
                page = new LoansPage().element();
                section = "loans";
            }
        }
        shell.show(section, page);
    }

    private static int parseId(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
