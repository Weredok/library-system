package dev.weredok.library.frontend.pages;

import dev.weredok.library.frontend.rest.ReaderServiceFactory;
import dev.weredok.library.frontend.ui.Api;
import dev.weredok.library.frontend.ui.Dom;
import dev.weredok.library.frontend.ui.Modal;
import dev.weredok.library.frontend.ui.Notifier;
import dev.weredok.library.frontend.ui.Ui;
import dev.weredok.library.shared.Validation;
import dev.weredok.library.shared.dto.ReaderDto;
import dev.weredok.library.shared.dto.ReaderFormDto;
import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.forms.TextBox;

import java.util.List;
import java.util.function.Consumer;

/** Реєстрація (existing == null) або редагування читача. Номер квитка видає сервер. */
public class ReaderDialog {

    private final Modal modal;
    private final TextBox fullName = Ui.textBox("ПІБ *");
    private final TextBox phone = Ui.textBox("Телефон");
    private final TextBox email = Ui.textBox("Email");
    private final Button save;
    private final ReaderDto existing;
    private final Consumer<ReaderDto> onSaved;

    public ReaderDialog(ReaderDto existing, Consumer<ReaderDto> onSaved) {
        this.existing = existing;
        this.onSaved = onSaved;
        modal = new Modal(existing == null ? "Реєстрація читача" : "Редагування читача");
        if (existing != null) {
            fullName.setValue(existing.getFullName());
            phone.setValue(existing.getPhone() == null ? "" : existing.getPhone());
            email.setValue(existing.getEmail() == null ? "" : existing.getEmail());
            modal.add(Dom.text("p", "muted", "Читацький квиток: " + existing.getTicketNumber()));
        } else {
            modal.add(Dom.text("p", "muted", "Номер читацького квитка буде згенеровано автоматично."));
        }
        save = Ui.primary("Зберегти", this::save);
        modal.add(fullName.element()).add(phone.element()).add(email.element())
                .button(Ui.button("Скасувати", modal::close))
                .button(save);
    }

    public void open() {
        modal.open();
    }

    private void save() {
        ReaderFormDto form = new ReaderFormDto();
        form.setFullName(Ui.value(fullName));
        form.setPhone(Ui.value(phone));
        form.setEmail(Ui.value(email));
        Validation.normalizeReader(form);
        List<String> errors = Validation.validateReader(form);
        if (!errors.isEmpty()) {
            Notifier.error(String.join("\n", errors));
            return;
        }
        Ui.setEnabled(save, false);
        Runnable reenable = () -> Ui.setEnabled(save, true);
        if (existing == null) {
            Api.send(ReaderServiceFactory.INSTANCE.register(form), saved -> {
                modal.close();
                Notifier.success("Зареєстровано. Квиток: " + saved.getTicketNumber());
                onSaved.accept(saved);
            }, reenable);
        } else {
            Api.send(ReaderServiceFactory.INSTANCE.update(existing.getId(), form), saved -> {
                modal.close();
                Notifier.success("Зміни збережено");
                onSaved.accept(saved);
            }, reenable);
        }
    }
}
