package dev.weredok.library.frontend.pages;

import dev.weredok.library.frontend.Router;
import dev.weredok.library.frontend.rest.ReaderServiceFactory;
import dev.weredok.library.frontend.ui.Api;
import dev.weredok.library.frontend.ui.Dom;
import dev.weredok.library.frontend.ui.Fmt;
import dev.weredok.library.frontend.ui.Pager;
import dev.weredok.library.frontend.ui.Table;
import dev.weredok.library.frontend.ui.Ui;
import dev.weredok.library.shared.dto.ReaderDto;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import org.dominokit.domino.ui.forms.TextBox;

public class ReadersPage {

    private static final int PAGE_SIZE = 20;

    private final HTMLElement root = Dom.div("page");
    private final TextBox search = Ui.textBox("Пошук: ПІБ, квиток, телефон, email");
    private final Table<ReaderDto> table = new Table<>();
    private final Pager pager = new Pager(this::load);
    private double searchTimer = -1;

    public ReadersPage() {
        HTMLElement head = Dom.div("page-head");
        head.appendChild(Dom.text("h1", null, "Читачі"));
        head.appendChild(Ui.primary("+ Зареєструвати", () ->
                new ReaderDialog(null, saved -> Router.go("reader/" + saved.getId())).open()).element());

        HTMLElement filters = Dom.div("filters");
        filters.appendChild(search.element());
        Ui.onInput(search, () -> {
            if (searchTimer >= 0) {
                DomGlobal.clearTimeout(searchTimer);
            }
            searchTimer = DomGlobal.setTimeout(ignored -> load(0), 300);
        });

        table.text("Квиток", ReaderDto::getTicketNumber)
                .column("ПІБ", r -> Dom.link(r.getFullName(), "#reader/" + r.getId()))
                .text("Телефон", r -> Fmt.orDash(r.getPhone()))
                .text("Email", r -> Fmt.orDash(r.getEmail()))
                .column("На руках", this::loansBadge)
                .column("", this::actions, "col-actions")
                .rowClass(r -> r.getOverdueLoans() > 0 ? "row-danger" : null);

        Dom.append(root, head, filters, table.element(), pager.element());
        load(0);
    }

    private HTMLElement loansBadge(ReaderDto r) {
        if (r.getOverdueLoans() > 0) {
            return Dom.badge(r.getActiveLoans() + " (прострочено " + r.getOverdueLoans() + ")", "danger");
        }
        return Dom.badge(String.valueOf(r.getActiveLoans()), r.getActiveLoans() > 0 ? "ok" : "muted");
    }

    private HTMLElement actions(ReaderDto r) {
        HTMLElement box = Dom.div("row-actions");
        box.appendChild(Ui.small("Картка", () -> Router.go("reader/" + r.getId())).element());
        box.appendChild(Ui.small("Видати книгу", () -> new IssueLoanDialog(r, null, () -> load(0)).open()).element());
        return box;
    }

    private void load(int page) {
        Api.send(ReaderServiceFactory.INSTANCE.search(Ui.value(search).trim(), page, PAGE_SIZE), result -> {
            table.setItems(result.getItems(), "Читачів не знайдено");
            pager.update(result.getPage(), result.getSize(), result.getTotal());
        });
    }

    public HTMLElement element() {
        return root;
    }
}
