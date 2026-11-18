package dev.weredok.library.frontend.pages;

import dev.weredok.library.frontend.Session;
import dev.weredok.library.frontend.rest.BookServiceFactory;
import dev.weredok.library.frontend.rest.CategoryServiceFactory;
import dev.weredok.library.frontend.ui.Api;
import dev.weredok.library.frontend.ui.Confirm;
import dev.weredok.library.frontend.ui.Dom;
import dev.weredok.library.frontend.ui.Fmt;
import dev.weredok.library.frontend.ui.Notifier;
import dev.weredok.library.frontend.ui.Pager;
import dev.weredok.library.frontend.ui.SelectBox;
import dev.weredok.library.frontend.ui.Table;
import dev.weredok.library.frontend.ui.Ui;
import dev.weredok.library.shared.dto.BookDto;
import dev.weredok.library.shared.dto.CategoryDto;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import org.dominokit.domino.ui.forms.CheckBox;
import org.dominokit.domino.ui.forms.TextBox;

import java.util.ArrayList;
import java.util.List;

public class BooksPage {

    private static final int PAGE_SIZE = 20;

    private final HTMLElement root = Dom.div("page");
    private final TextBox search = Ui.textBox("Пошук: назва, автор, ISBN");
    private final SelectBox category = new SelectBox("Категорія");
    private final CheckBox availableOnly = Ui.checkBox("Тільки в наявності", () -> load(0));
    private final Table<BookDto> table = new Table<>();
    private final Pager pager = new Pager(this::load);
    private List<CategoryDto> categories = new ArrayList<>();
    private double searchTimer = -1;

    public BooksPage() {
        HTMLElement head = Dom.div("page-head");
        head.appendChild(Dom.text("h1", null, "Книжковий фонд"));
        HTMLElement headActions = Dom.div("row-actions");
        if (Session.isAdmin()) {
            headActions.appendChild(Ui.button("Категорії", () ->
                    new CategoriesDialog(this::loadCategories).open()).element());
        }
        headActions.appendChild(Ui.primary("+ Додати книгу", () ->
                new BookDialog(null, categories, () -> load(0)).open()).element());
        head.appendChild(headActions);

        HTMLElement filters = Dom.div("filters");
        Dom.append(filters, search.element(), category.element(), availableOnly.element());
        Ui.onInput(search, this::debouncedSearch);
        category.onChange(() -> load(0));

        table.column("Назва", b -> Dom.text("strong", null, b.getTitle()))
                .text("Автор", BookDto::getAuthor)
                .text("ISBN", b -> Fmt.orDash(b.getIsbn()))
                .text("Рік", b -> Fmt.orDash(b.getYear()))
                .text("Категорія", b -> Fmt.orDash(b.getCategoryName()))
                .column("Вільно", b -> Dom.badge(b.getAvailableCopies() + " / " + b.getTotalCopies(),
                        b.getAvailableCopies() > 0 ? "ok" : "danger"))
                .column("", this::actions, "col-actions");

        Dom.append(root, head, filters, table.element(), pager.element());
        loadCategories();
        load(0);
    }

    private HTMLElement actions(BookDto b) {
        HTMLElement box = Dom.div("row-actions");
        if (b.getAvailableCopies() > 0) {
            box.appendChild(Ui.small("Видати", () -> new IssueLoanDialog(null, b, () -> load(0)).open()).element());
        }
        box.appendChild(Ui.small("Редагувати", () -> new BookDialog(b, categories, () -> load(0)).open()).element());
        if (Session.isAdmin()) {
            box.appendChild(Ui.small("Списати", () -> Confirm.ask("Списання книги",
                    "«" + b.getTitle() + "» зникне з фонду. Історія видач збережеться.",
                    "Списати",
                    () -> Api.send(BookServiceFactory.INSTANCE.archive(b.getId()), ok -> {
                        Notifier.success("Книгу списано");
                        load(0);
                    }))).element());
        }
        return box;
    }

    private void debouncedSearch() {
        if (searchTimer >= 0) {
            DomGlobal.clearTimeout(searchTimer);
        }
        searchTimer = DomGlobal.setTimeout(ignored -> load(0), 300);
    }

    private void loadCategories() {
        Api.send(CategoryServiceFactory.INSTANCE.list(), list -> {
            categories = list;
            String current = category.getValue();
            category.clearOptions();
            category.option("", "Усі");
            for (CategoryDto c : list) {
                category.option(String.valueOf(c.getId()), c.getName());
            }
            category.setValue(current);
        });
    }

    private void load(int page) {
        String cat = category.getValue();
        int categoryId = cat == null || cat.isEmpty() ? 0 : Integer.parseInt(cat);
        Api.send(BookServiceFactory.INSTANCE.search(Ui.value(search).trim(), categoryId, Ui.checked(availableOnly), page, PAGE_SIZE),
                result -> {
                    table.setItems(result.getItems(), "Книг не знайдено");
                    pager.update(result.getPage(), result.getSize(), result.getTotal());
                });
    }

    public HTMLElement element() {
        return root;
    }
}
