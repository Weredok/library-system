package dev.weredok.library.frontend.pages;

import dev.weredok.library.frontend.rest.CategoryServiceFactory;
import dev.weredok.library.frontend.ui.Api;
import dev.weredok.library.frontend.ui.Confirm;
import dev.weredok.library.frontend.ui.Dom;
import dev.weredok.library.frontend.ui.Modal;
import dev.weredok.library.frontend.ui.Notifier;
import dev.weredok.library.frontend.ui.Table;
import dev.weredok.library.frontend.ui.Ui;
import dev.weredok.library.shared.dto.CategoryDto;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import org.dominokit.domino.ui.forms.TextBox;

/** Керування категоріями (тільки ADMIN — сервер перевіряє). */
public class CategoriesDialog {

    private final Modal modal = new Modal("Категорії");
    private final Table<CategoryDto> table = new Table<>();
    private final TextBox newName = Ui.textBox("Нова категорія");
    private final Runnable onChanged;

    public CategoriesDialog(Runnable onChanged) {
        this.onChanged = onChanged;
        table.text("Назва", CategoryDto::getName)
                .column("", this::actions, "col-actions");

        HTMLElement addRow = Dom.div("inline-form");
        Dom.append(addRow, newName.element(), Ui.primary("Додати", this::add).element());
        Ui.onEnter(newName, this::add);

        modal.add(addRow).add(table.element()).button(Ui.button("Закрити", () -> {
            modal.close();
            onChanged.run();
        }));
        load();
    }

    public void open() {
        modal.open();
    }

    private void load() {
        Api.send(CategoryServiceFactory.INSTANCE.list(), list -> table.setItems(list, "Категорій ще немає"));
    }

    private HTMLElement actions(CategoryDto c) {
        HTMLElement box = Dom.div("row-actions");
        box.appendChild(Ui.small("Перейменувати", () -> {
            String name = DomGlobal.window.prompt("Нова назва", c.getName());
            if (name != null && !name.trim().isEmpty()) {
                CategoryDto body = new CategoryDto();
                body.setName(name);
                Api.send(CategoryServiceFactory.INSTANCE.rename(c.getId(), body), ok -> load());
            }
        }).element());
        box.appendChild(Ui.small("Видалити", () -> Confirm.ask("Видалити категорію",
                "Категорію «" + c.getName() + "» буде видалено, її книги залишаться без категорії.",
                "Видалити",
                () -> Api.send(CategoryServiceFactory.INSTANCE.delete(c.getId()), ok -> load()))).element());
        return box;
    }

    private void add() {
        String name = Ui.value(newName).trim();
        if (name.isEmpty()) {
            return;
        }
        CategoryDto body = new CategoryDto();
        body.setName(name);
        Api.send(CategoryServiceFactory.INSTANCE.create(body), created -> {
            newName.setValue("");
            Notifier.success("Категорію додано");
            load();
        });
    }
}
