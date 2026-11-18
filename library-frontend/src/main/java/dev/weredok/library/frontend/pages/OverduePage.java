package dev.weredok.library.frontend.pages;

import dev.weredok.library.frontend.rest.OverdueServiceFactory;
import dev.weredok.library.frontend.ui.Api;
import dev.weredok.library.frontend.ui.Dom;
import dev.weredok.library.frontend.ui.Fmt;
import dev.weredok.library.frontend.ui.Table;
import dev.weredok.library.frontend.ui.Ui;
import dev.weredok.library.shared.dto.OverdueDto;
import elemental2.dom.HTMLElement;

/** Боржники: усі неповернені книги з минулим терміном, найдовші прострочки зверху. */
public class OverduePage {

    private final HTMLElement root = Dom.div("page");
    private final HTMLElement summary = Dom.text("p", "muted", "");
    private final Table<OverdueDto> table = new Table<>();

    public OverduePage() {
        HTMLElement head = Dom.div("page-head");
        head.appendChild(Dom.text("h1", null, "Боржники"));
        head.appendChild(Ui.button("Оновити", this::load).element());

        table.column("Читач", o -> Dom.append(Dom.div(null),
                        Dom.link(o.getReaderName(), "#reader/" + o.getReaderId()),
                        Dom.text("div", "muted", o.getTicketNumber())))
                .column("Контакти", o -> Dom.append(Dom.div(null),
                        Dom.text("div", null, Fmt.orDash(o.getPhone())),
                        Dom.text("div", "muted", Fmt.orDash(o.getEmail()))))
                .column("Книга", o -> Dom.text("strong", null, o.getBookTitle()))
                .text("Видано", o -> Fmt.date(o.getIssueDate()))
                .text("Мало бути повернено", o -> Fmt.date(o.getDueDate()))
                .column("Прострочено", o -> Dom.badge(Fmt.days(o.getDaysOverdue()),
                        o.getDaysOverdue() > 14 ? "danger" : "warn"))
                .column("", o -> Ui.small("Повернути", () -> LoanActions.returnLoan(
                        o.getLoanId(), o.getBookTitle(), o.getReaderName(), this::load)).element(), "col-actions");

        Dom.append(root, head, summary, table.element());
        load();
    }

    private void load() {
        Api.send(OverdueServiceFactory.INSTANCE.list(), list -> {
            summary.textContent = list.isEmpty()
                    ? "Прострочених видач немає 🎉"
                    : "Прострочених видач: " + list.size();
            table.setItems(list, "Боржників немає");
        });
    }

    public HTMLElement element() {
        return root;
    }
}
