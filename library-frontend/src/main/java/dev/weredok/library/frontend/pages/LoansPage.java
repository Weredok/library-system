package dev.weredok.library.frontend.pages;

import dev.weredok.library.frontend.rest.LoanServiceFactory;
import dev.weredok.library.frontend.ui.Api;
import dev.weredok.library.frontend.ui.Dom;
import dev.weredok.library.frontend.ui.Fmt;
import dev.weredok.library.frontend.ui.Pager;
import dev.weredok.library.frontend.ui.SelectBox;
import dev.weredok.library.frontend.ui.Table;
import dev.weredok.library.frontend.ui.Ui;
import dev.weredok.library.shared.LoanFilter;
import dev.weredok.library.shared.LoanStatus;
import dev.weredok.library.shared.dto.LoanDto;
import elemental2.dom.HTMLElement;

/** Абонемент: журнал видач з фільтром, видача, повернення, продовження. */
public class LoansPage {

    private static final int PAGE_SIZE = 20;

    private final HTMLElement root = Dom.div("page");
    private final SelectBox filter = new SelectBox("Показати");
    private final Table<LoanDto> table = new Table<>();
    private final Pager pager = new Pager(this::load);
    private int currentPage;

    public LoansPage() {
        HTMLElement head = Dom.div("page-head");
        head.appendChild(Dom.text("h1", null, "Абонемент"));
        head.appendChild(Ui.primary("+ Видати книгу", () -> new IssueLoanDialog(null, null, () -> load(0)).open()).element());

        for (LoanFilter f : LoanFilter.values()) {
            filter.option(f.name(), f.getLabel());
        }
        filter.setValue(LoanFilter.ON_HAND.name());
        filter.onChange(() -> load(0));
        HTMLElement filters = Dom.div("filters");
        filters.appendChild(filter.element());

        table.text("Видано", l -> Fmt.date(l.getIssueDate()))
                .column("Книга", l -> Dom.append(Dom.div(null),
                        Dom.text("strong", null, l.getBookTitle()),
                        Dom.text("div", "muted", l.getBookAuthor())))
                .column("Читач", l -> Dom.append(Dom.div(null),
                        Dom.link(l.getReaderName(), "#reader/" + l.getReaderId()),
                        Dom.text("div", "muted", l.getTicketNumber())))
                .text("Повернути до", l -> Fmt.date(l.getDueDate())
                        + (l.getRenewalCount() > 0 ? " (продовжено)" : ""))
                .column("Статус", LoanActions::statusBadge)
                .text("Повернено", l -> Fmt.date(l.getReturnDate()))
                .text("Видав / прийняв", l -> Fmt.orDash(l.getIssuedBy())
                        + (l.getReceivedBy() != null ? " / " + l.getReceivedBy() : ""))
                .column("", l -> LoanActions.buttons(l, () -> load(currentPage)), "col-actions")
                .rowClass(l -> l.getStatus() == LoanStatus.OVERDUE ? "row-danger" : null);

        Dom.append(root, head, filters, table.element(), pager.element());
        load(0);
    }

    private void load(int page) {
        currentPage = page;
        Api.send(LoanServiceFactory.INSTANCE.search(filter.getValue(), 0, 0, page, PAGE_SIZE), result -> {
            table.setItems(result.getItems(), "Записів немає");
            pager.update(result.getPage(), result.getSize(), result.getTotal());
        });
    }

    public HTMLElement element() {
        return root;
    }
}
