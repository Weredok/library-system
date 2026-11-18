package dev.weredok.library.frontend.pages;

import dev.weredok.library.frontend.rest.BookServiceFactory;
import dev.weredok.library.frontend.rest.LoanServiceFactory;
import dev.weredok.library.frontend.rest.ReaderServiceFactory;
import dev.weredok.library.frontend.ui.Api;
import dev.weredok.library.frontend.ui.Dom;
import dev.weredok.library.frontend.ui.Fmt;
import dev.weredok.library.frontend.ui.Modal;
import dev.weredok.library.frontend.ui.Notifier;
import dev.weredok.library.frontend.ui.Picker;
import dev.weredok.library.frontend.ui.Ui;
import dev.weredok.library.shared.LibraryRules;
import dev.weredok.library.shared.dto.BookDto;
import dev.weredok.library.shared.dto.IssueLoanRequest;
import dev.weredok.library.shared.dto.ReaderDto;
import elemental2.dom.HTMLElement;
import org.dominokit.domino.ui.button.Button;

/**
 * Оформлення видачі: вибрати читача і книгу. Попередження тут — лише підказки,
 * остаточне рішення приймає сервер (там самі правила, але в транзакції з блокуванням).
 */
public class IssueLoanDialog {

    private final Modal modal = new Modal("Видача книги");
    private final Picker<ReaderDto> readerPicker;
    private final Picker<BookDto> bookPicker;
    private final HTMLElement warnings = Dom.div("warnings");
    private final Button submit;
    private final Runnable onIssued;

    public IssueLoanDialog(ReaderDto presetReader, BookDto presetBook, Runnable onIssued) {
        this.onIssued = onIssued;

        readerPicker = new Picker<>("Читач (ПІБ, квиток, телефон)",
                (q, cb) -> Api.send(ReaderServiceFactory.INSTANCE.search(q, 0, 8), page -> cb.accept(page.getItems())),
                r -> r.getFullName() + " · " + r.getTicketNumber(),
                r -> "На руках: " + r.getActiveLoans()
                        + (r.getOverdueLoans() > 0 ? " · прострочено: " + r.getOverdueLoans() : ""));
        bookPicker = new Picker<>("Книга (назва, автор, ISBN)",
                (q, cb) -> Api.send(BookServiceFactory.INSTANCE.search(q, 0, false, 0, 8), page -> cb.accept(page.getItems())),
                b -> b.getTitle() + " — " + b.getAuthor(),
                b -> "Вільно: " + b.getAvailableCopies() + " з " + b.getTotalCopies());
        readerPicker.onSelect(r -> updateWarnings());
        bookPicker.onSelect(b -> updateWarnings());

        submit = Ui.primary("Видати", this::issue);
        modal.add(readerPicker.element())
                .add(bookPicker.element())
                .add(Dom.text("p", "muted",
                        "Термін: " + Fmt.days(LibraryRules.LOAN_DAYS) + ". Ліміт: "
                                + LibraryRules.MAX_ACTIVE_LOANS_PER_READER + " книг на руках."))
                .add(warnings)
                .button(Ui.button("Скасувати", modal::close))
                .button(submit);

        if (presetReader != null) {
            readerPicker.select(presetReader);
        }
        if (presetBook != null) {
            bookPicker.select(presetBook);
        }
        updateWarnings();
    }

    public void open() {
        modal.open();
    }

    private void updateWarnings() {
        Dom.clear(warnings);
        ReaderDto r = readerPicker.getSelected();
        BookDto b = bookPicker.getSelected();
        boolean blocked = r == null || b == null;
        if (r != null && r.getOverdueLoans() > 0) {
            warnings.appendChild(Dom.badge("У читача є прострочені книги — видача заблокована", "danger"));
            blocked = true;
        }
        if (r != null && r.getActiveLoans() >= LibraryRules.MAX_ACTIVE_LOANS_PER_READER) {
            warnings.appendChild(Dom.badge("Читач досяг ліміту книг", "danger"));
            blocked = true;
        }
        if (b != null && b.getAvailableCopies() <= 0) {
            warnings.appendChild(Dom.badge("Немає вільних примірників", "danger"));
            blocked = true;
        }
        Ui.setEnabled(submit, !blocked);
    }

    private void issue() {
        ReaderDto r = readerPicker.getSelected();
        BookDto b = bookPicker.getSelected();
        if (r == null || b == null) {
            return;
        }
        IssueLoanRequest req = new IssueLoanRequest();
        req.setReaderId(r.getId());
        req.setBookId(b.getId());
        Ui.setEnabled(submit, false);
        Api.send(LoanServiceFactory.INSTANCE.issue(req), loan -> {
            modal.close();
            Notifier.success("Видано «" + loan.getBookTitle() + "» до " + Fmt.date(loan.getDueDate()));
            onIssued.run();
        }, () -> Ui.setEnabled(submit, true));
    }
}
