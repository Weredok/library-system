package dev.weredok.library.frontend.pages;

import dev.weredok.library.frontend.Router;
import dev.weredok.library.frontend.Session;
import dev.weredok.library.frontend.rest.ReaderServiceFactory;
import dev.weredok.library.frontend.ui.Api;
import dev.weredok.library.frontend.ui.Confirm;
import dev.weredok.library.frontend.ui.Dom;
import dev.weredok.library.frontend.ui.Fmt;
import dev.weredok.library.frontend.ui.Notifier;
import dev.weredok.library.frontend.ui.Pager;
import dev.weredok.library.frontend.ui.Table;
import dev.weredok.library.frontend.ui.Ui;
import dev.weredok.library.shared.LoanStatus;
import dev.weredok.library.shared.dto.LoanDto;
import dev.weredok.library.shared.dto.ReaderDto;
import elemental2.dom.HTMLElement;

/** Картка читача: контакти, лічильники, історія видач. */
public class ReaderDetailsPage {

    private static final int PAGE_SIZE = 15;

    private final int readerId;
    private final HTMLElement root = Dom.div("page");
    private final HTMLElement card = Dom.div("card");
    private final Table<LoanDto> history = new Table<>();
    private final Pager pager = new Pager(this::loadHistory);
    private int currentPage;

    public ReaderDetailsPage(int readerId) {
        this.readerId = readerId;
        HTMLElement back = Dom.link("← Усі читачі", "#readers");
        back.className = "back-link";

        history.text("Видано", l -> Fmt.date(l.getIssueDate()))
                .column("Книга", l -> Dom.text("strong", null, l.getBookTitle()))
                .text("Повернути до", l -> Fmt.date(l.getDueDate()))
                .text("Повернено", l -> Fmt.date(l.getReturnDate()))
                .column("Статус", LoanActions::statusBadge)
                .text("Видав / прийняв", l -> Fmt.orDash(l.getIssuedBy())
                        + (l.getReceivedBy() != null ? " / " + l.getReceivedBy() : ""))
                .column("", l -> LoanActions.buttons(l, this::reload), "col-actions")
                .rowClass(l -> l.getStatus() == LoanStatus.OVERDUE ? "row-danger" : null);

        Dom.append(root, back, card,
                Dom.text("h2", null, "Історія видач"),
                history.element(), pager.element());
        reload();
    }

    private void reload() {
        loadReader();
        loadHistory(currentPage);
    }

    private void loadReader() {
        Api.send(ReaderServiceFactory.INSTANCE.get(readerId), this::renderCard);
    }

    private void renderCard(ReaderDto r) {
        Dom.clear(card);
        HTMLElement head = Dom.div("page-head");
        head.appendChild(Dom.text("h1", null, r.getFullName()));
        HTMLElement actions = Dom.div("row-actions");
        actions.appendChild(Ui.primary("Видати книгу", () -> new IssueLoanDialog(r, null, this::reload).open()).element());
        actions.appendChild(Ui.button("Редагувати", () -> new ReaderDialog(r, saved -> reload()).open()).element());
        if (Session.isAdmin()) {
            actions.appendChild(Ui.danger("Архівувати", () -> Confirm.ask("Архівування читача",
                    r.getFullName() + " зникне зі списку читачів. Історія видач збережеться.",
                    "Архівувати",
                    () -> Api.send(ReaderServiceFactory.INSTANCE.archive(readerId), ok -> {
                        Notifier.success("Читача архівовано");
                        Router.go("readers");
                    }))).element());
        }
        head.appendChild(actions);
        card.appendChild(head);

        HTMLElement info = Dom.div("info-grid");
        addInfo(info, "Читацький квиток", r.getTicketNumber());
        addInfo(info, "Телефон", Fmt.orDash(r.getPhone()));
        addInfo(info, "Email", Fmt.orDash(r.getEmail()));
        addInfo(info, "Зареєстровано", Fmt.date(r.getRegisteredAt()));
        addInfo(info, "На руках", String.valueOf(r.getActiveLoans()));
        card.appendChild(info);

        if (r.getOverdueLoans() > 0) {
            card.appendChild(Dom.badge("Прострочених книг: " + r.getOverdueLoans() + " — нова видача заблокована", "danger"));
        }
    }

    private static void addInfo(HTMLElement grid, String label, String value) {
        HTMLElement item = Dom.div("info-item");
        item.appendChild(Dom.text("div", "info-label", label));
        item.appendChild(Dom.text("div", "info-value", value));
        grid.appendChild(item);
    }

    private void loadHistory(int page) {
        currentPage = page;
        Api.send(ReaderServiceFactory.INSTANCE.history(readerId, page, PAGE_SIZE), result -> {
            history.setItems(result.getItems(), "Цей читач ще не брав книг");
            pager.update(result.getPage(), result.getSize(), result.getTotal());
        });
    }

    public HTMLElement element() {
        return root;
    }
}
