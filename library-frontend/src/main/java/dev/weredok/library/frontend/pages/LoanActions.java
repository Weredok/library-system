package dev.weredok.library.frontend.pages;

import dev.weredok.library.frontend.rest.LoanServiceFactory;
import dev.weredok.library.frontend.ui.Api;
import dev.weredok.library.frontend.ui.Confirm;
import dev.weredok.library.frontend.ui.Dom;
import dev.weredok.library.frontend.ui.Fmt;
import dev.weredok.library.frontend.ui.Notifier;
import dev.weredok.library.frontend.ui.Ui;
import dev.weredok.library.shared.LoanStatus;
import dev.weredok.library.shared.dto.LoanDto;
import elemental2.dom.HTMLElement;

/** Кнопки «Повернути» / «Продовжити» і бейдж статусу — однакові на кількох сторінках. */
final class LoanActions {

    private LoanActions() {
    }

    static HTMLElement statusBadge(LoanDto l) {
        LoanStatus s = l.getStatus();
        if (s == LoanStatus.OVERDUE) {
            return Dom.badge("Прострочено " + Fmt.days(l.getDaysOverdue()), "danger");
        }
        if (s == LoanStatus.RETURNED) {
            return Dom.badge(s.getLabel(), "muted");
        }
        return Dom.badge(s.getLabel(), "ok");
    }

    static HTMLElement buttons(LoanDto l, Runnable onChanged) {
        HTMLElement box = Dom.div("row-actions");
        if (l.getStatus() == LoanStatus.RETURNED) {
            return box;
        }
        box.appendChild(Ui.small("Повернути", () -> returnLoan(l.getId(), l.getBookTitle(), l.getReaderName(), onChanged)).element());
        if (l.isCanRenew()) {
            box.appendChild(Ui.small("Продовжити", () ->
                    Api.send(LoanServiceFactory.INSTANCE.renew(l.getId()), updated -> {
                        Notifier.success("Продовжено до " + Fmt.date(updated.getDueDate()));
                        onChanged.run();
                    })).element());
        }
        return box;
    }

    static void returnLoan(int loanId, String bookTitle, String readerName, Runnable onChanged) {
        Confirm.ask("Повернення книги",
                "«" + bookTitle + "» від читача " + readerName + " — прийняти?",
                "Прийняти",
                () -> Api.send(LoanServiceFactory.INSTANCE.returnBook(loanId), updated -> {
                    Notifier.success("Книгу повернено");
                    onChanged.run();
                }));
    }
}
