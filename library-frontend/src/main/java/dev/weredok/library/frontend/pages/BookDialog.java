package dev.weredok.library.frontend.pages;

import dev.weredok.library.frontend.rest.BookServiceFactory;
import dev.weredok.library.frontend.ui.Api;
import dev.weredok.library.frontend.ui.Dom;
import dev.weredok.library.frontend.ui.Modal;
import dev.weredok.library.frontend.ui.Notifier;
import dev.weredok.library.frontend.ui.SelectBox;
import dev.weredok.library.frontend.ui.Ui;
import dev.weredok.library.shared.Validation;
import dev.weredok.library.shared.dto.BookDto;
import dev.weredok.library.shared.dto.BookFormDto;
import dev.weredok.library.shared.dto.CategoryDto;
import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.forms.IntegerBox;
import org.dominokit.domino.ui.forms.TextBox;

import java.util.List;

/** Створення (existing == null) або редагування книги. */
public class BookDialog {

    private final Modal modal;
    private final TextBox title = Ui.textBox("Назва *");
    private final TextBox author = Ui.textBox("Автор *");
    private final TextBox isbn = Ui.textBox("ISBN (10 або 13 цифр)");
    private final IntegerBox year = Ui.intBox("Рік видання");
    private final IntegerBox copies = Ui.intBox("Кількість примірників *");
    private final SelectBox category = new SelectBox("Категорія");
    private final Button save;
    private final BookDto existing;
    private final Runnable onSaved;

    public BookDialog(BookDto existing, List<CategoryDto> categories, Runnable onSaved) {
        this.existing = existing;
        this.onSaved = onSaved;
        modal = new Modal(existing == null ? "Нова книга" : "Редагування книги");

        category.option("", "— без категорії —");
        for (CategoryDto c : categories) {
            category.option(String.valueOf(c.getId()), c.getName());
        }

        if (existing != null) {
            title.setValue(existing.getTitle());
            author.setValue(existing.getAuthor());
            isbn.setValue(existing.getIsbn() == null ? "" : existing.getIsbn());
            year.setValue(existing.getYear());
            copies.setValue(existing.getTotalCopies());
            category.setValue(existing.getCategoryId() == null ? "" : String.valueOf(existing.getCategoryId()));
        } else {
            copies.setValue(1);
        }

        save = Ui.primary("Зберегти", this::save);
        modal.add(title.element())
                .add(author.element())
                .add(isbn.element())
                .add(year.element())
                .add(category.element())
                .add(copies.element());
        if (existing != null) {
            int onLoan = existing.getTotalCopies() - existing.getAvailableCopies();
            modal.add(Dom.text("p", "muted", "Зараз видано: " + onLoan + " — менше не можна"));
        }
        modal.button(Ui.button("Скасувати", modal::close)).button(save);
    }

    public void open() {
        modal.open();
    }

    private void save() {
        BookFormDto form = new BookFormDto();
        form.setTitle(Ui.value(title));
        form.setAuthor(Ui.value(author));
        form.setIsbn(Ui.value(isbn));
        form.setYear(year.getValue());
        form.setTotalCopies(copies.getValue() == null ? -1 : copies.getValue());
        String cat = category.getValue();
        form.setCategoryId(cat.isEmpty() ? null : Integer.valueOf(cat));

        // Та сама валідація, що й на сервері — помилку видно одразу, без запиту
        Validation.normalizeBook(form);
        List<String> errors = Validation.validateBook(form);
        if (!errors.isEmpty()) {
            Notifier.error(String.join("\n", errors));
            return;
        }

        Ui.setEnabled(save, false);
        Runnable reenable = () -> Ui.setEnabled(save, true);
        if (existing == null) {
            Api.send(BookServiceFactory.INSTANCE.create(form), saved -> done("Книгу додано"), reenable);
        } else {
            Api.send(BookServiceFactory.INSTANCE.update(existing.getId(), form), saved -> done("Зміни збережено"), reenable);
        }
    }

    private void done(String message) {
        modal.close();
        Notifier.success(message);
        onSaved.run();
    }
}
