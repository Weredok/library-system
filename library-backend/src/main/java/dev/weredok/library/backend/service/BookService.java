package dev.weredok.library.backend.service;

import dev.weredok.library.backend.domain.Book;
import dev.weredok.library.backend.domain.Category;
import dev.weredok.library.backend.error.BusinessException;
import dev.weredok.library.backend.error.NotFoundException;
import dev.weredok.library.backend.mapper.DtoMapper;
import dev.weredok.library.backend.repository.BookRepository;
import dev.weredok.library.backend.repository.CategoryRepository;
import dev.weredok.library.backend.repository.LoanRepository;
import dev.weredok.library.backend.repository.Specs;
import dev.weredok.library.shared.ErrorCode;
import dev.weredok.library.shared.Validation;
import dev.weredok.library.shared.dto.BookDto;
import dev.weredok.library.shared.dto.BookFormDto;
import dev.weredok.library.shared.dto.BookPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookService {

    private final BookRepository books;
    private final CategoryRepository categories;
    private final LoanRepository loans;
    private final Clock clock;

    public BookService(BookRepository books, CategoryRepository categories, LoanRepository loans, Clock clock) {
        this.books = books;
        this.categories = categories;
        this.loans = loans;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public BookPage search(String q, Integer categoryId, boolean availableOnly, int page, int size) {
        Page<Book> result = books.findAll(
                Specs.books(Validation.trimToNull(q), categoryId, availableOnly),
                Paging.of(page, size, Sort.by("title").and(Sort.by("id"))));

        // Один запит на всю сторінку замість окремого підрахунку на кожну книгу (N+1)
        List<Integer> ids = result.getContent().stream().map(Book::getId).toList();
        Map<Integer, Long> active = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] row : loans.countActiveByBookIds(ids)) {
                active.put((Integer) row[0], (Long) row[1]);
            }
        }

        BookPage dto = new BookPage();
        dto.setItems(result.getContent().stream()
                .map(b -> DtoMapper.book(b, active.getOrDefault(b.getId(), 0L)))
                .toList());
        dto.setPage(result.getNumber());
        dto.setSize(result.getSize());
        dto.setTotal(result.getTotalElements());
        return dto;
    }

    @Transactional(readOnly = true)
    public BookDto get(Integer id) {
        Book b = find(id);
        return DtoMapper.book(b, loans.countActiveByBook(id));
    }

    @Transactional
    public BookDto create(BookFormDto form) {
        validate(form);
        if (form.getIsbn() != null && books.existsByIsbn(form.getIsbn())) {
            throw new BusinessException(ErrorCode.ISBN_EXISTS);
        }
        Book b = new Book();
        apply(b, form);
        books.save(b);
        return DtoMapper.book(b, 0);
    }

    @Transactional
    public BookDto update(Integer id, BookFormDto form) {
        validate(form);
        // Блокуємо книгу: інакше між перевіркою «виданих N» і збереженням хтось міг видати ще одну
        Book b = books.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Книгу", id));
        if (b.isArchived()) {
            throw new BusinessException(ErrorCode.BOOK_ARCHIVED);
        }
        if (form.getIsbn() != null && books.existsByIsbnAndIdNot(form.getIsbn(), id)) {
            throw new BusinessException(ErrorCode.ISBN_EXISTS);
        }
        long onLoan = loans.countActiveByBook(id);
        if (form.getTotalCopies() < onLoan) {
            throw new BusinessException(ErrorCode.TOTAL_BELOW_ON_LOAN,
                    ErrorCode.TOTAL_BELOW_ON_LOAN.getDefaultMessage() + " (зараз видано: " + onLoan + ")");
        }
        apply(b, form);
        return DtoMapper.book(b, onLoan);
    }

    /**
     * «Видалення» = списання. Рядок лишається в БД, бо на нього посилається історія видач.
     */
    @Transactional
    public void archive(Integer id) {
        Book b = books.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Книгу", id));
        if (b.isArchived()) {
            return;
        }
        if (loans.countActiveByBook(id) > 0) {
            throw new BusinessException(ErrorCode.BOOK_HAS_ACTIVE_LOANS);
        }
        b.setArchivedAt(OffsetDateTime.now(clock));
    }

    private Book find(Integer id) {
        Book b = books.findById(id).orElseThrow(() -> new NotFoundException("Книгу", id));
        if (b.isArchived()) {
            throw new NotFoundException("Книгу", id);
        }
        return b;
    }

    private static void validate(BookFormDto form) {
        Validation.normalizeBook(form);
        BusinessException.throwIfInvalid(Validation.validateBook(form));
    }

    private void apply(Book b, BookFormDto form) {
        b.setTitle(form.getTitle());
        b.setAuthor(form.getAuthor());
        b.setIsbn(form.getIsbn());
        b.setYear(form.getYear());
        b.setTotalCopies(form.getTotalCopies());
        Category category = null;
        if (form.getCategoryId() != null) {
            category = categories.findById(form.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Категорію", form.getCategoryId()));
        }
        b.setCategory(category);
    }
}
