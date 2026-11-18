package dev.weredok.library.backend.service;

import dev.weredok.library.backend.domain.AppUser;
import dev.weredok.library.backend.domain.Book;
import dev.weredok.library.backend.domain.Loan;
import dev.weredok.library.backend.domain.Reader;
import dev.weredok.library.backend.error.BusinessException;
import dev.weredok.library.backend.repository.AppUserRepository;
import dev.weredok.library.backend.repository.BookRepository;
import dev.weredok.library.backend.repository.LoanRepository;
import dev.weredok.library.backend.repository.ReaderRepository;
import dev.weredok.library.shared.ErrorCode;
import dev.weredok.library.shared.LibraryRules;
import dev.weredok.library.shared.LoanStatus;
import dev.weredok.library.shared.dto.IssueLoanRequest;
import dev.weredok.library.shared.dto.LoanDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Правила абонементу без БД: репозиторії замокані, дата зафіксована. */
class LoanServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 18);

    private LoanRepository loans;
    private BookRepository books;
    private ReaderRepository readers;
    private AppUserRepository users;
    private LoanService service;

    private Book book;
    private Reader reader;
    private AppUser librarian;

    @BeforeEach
    void setUp() {
        loans = mock(LoanRepository.class);
        books = mock(BookRepository.class);
        readers = mock(ReaderRepository.class);
        users = mock(AppUserRepository.class);
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.of("UTC"));
        service = new LoanService(loans, books, readers, users, clock);

        book = new Book();
        ReflectionTestUtils.setField(book, "id", 10);
        book.setTitle("Дюна");
        book.setAuthor("Френк Герберт");
        book.setTotalCopies(1);

        reader = new Reader("R-2026-000001", TODAY.minusDays(30));
        ReflectionTestUtils.setField(reader, "id", 20);
        reader.setFullName("Тест Читач");

        librarian = new AppUser("librarian", "x", AppUser.Role.LIBRARIAN);
        ReflectionTestUtils.setField(librarian, "id", 1);

        when(readers.findByIdForUpdate(20)).thenReturn(Optional.of(reader));
        when(books.findByIdForUpdate(10)).thenReturn(Optional.of(book));
        when(users.findById(1)).thenReturn(Optional.of(librarian));
    }

    private IssueLoanRequest request() {
        IssueLoanRequest r = new IssueLoanRequest();
        r.setReaderId(20);
        r.setBookId(10);
        return r;
    }

    @Test
    void issuesBookWithDefaultTerm() {
        LoanDto dto = service.issue(request(), 1);

        assertThat(dto.getIssueDate()).isEqualTo("2026-11-18");
        assertThat(dto.getDueDate()).isEqualTo(TODAY.plusDays(LibraryRules.LOAN_DAYS).toString());
        assertThat(dto.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(dto.getIssuedBy()).isEqualTo("librarian");
        verify(loans).save(any(Loan.class));
    }

    @Test
    void refusesWhenNoFreeCopies() {
        when(loans.countActiveByBook(10)).thenReturn(1L);

        assertError(() -> service.issue(request(), 1), ErrorCode.NO_COPIES_AVAILABLE);
        verify(loans, never()).save(any());
    }

    @Test
    void refusesWhenReaderHasOverdue() {
        when(loans.hasOverdue(20, TODAY)).thenReturn(true);

        assertError(() -> service.issue(request(), 1), ErrorCode.READER_HAS_OVERDUE);
    }

    @Test
    void refusesWhenLimitReached() {
        when(loans.countActiveByReader(20)).thenReturn((long) LibraryRules.MAX_ACTIVE_LOANS_PER_READER);

        assertError(() -> service.issue(request(), 1), ErrorCode.LOAN_LIMIT_REACHED);
    }

    @Test
    void refusesSameBookTwice() {
        when(loans.existsActive(20, 10)).thenReturn(true);

        assertError(() -> service.issue(request(), 1), ErrorCode.BOOK_ALREADY_ON_HAND);
    }

    @Test
    void refusesArchivedBook() {
        book.setArchivedAt(java.time.OffsetDateTime.now());

        assertError(() -> service.issue(request(), 1), ErrorCode.BOOK_ARCHIVED);
    }

    @Test
    void returnsBookOnceOnly() {
        Loan loan = new Loan(book, reader, librarian, TODAY.minusDays(3), TODAY.plusDays(11));
        ReflectionTestUtils.setField(loan, "id", 5);
        when(loans.findByIdForUpdate(5)).thenReturn(Optional.of(loan));

        LoanDto dto = service.returnBook(5, 1);
        assertThat(dto.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(dto.getReceivedBy()).isEqualTo("librarian");

        assertError(() -> service.returnBook(5, 1), ErrorCode.LOAN_ALREADY_RETURNED);
    }

    @Test
    void renewsOnceAndNotWhenOverdue() {
        Loan loan = new Loan(book, reader, librarian, TODAY.minusDays(3), TODAY.plusDays(11));
        ReflectionTestUtils.setField(loan, "id", 5);
        when(loans.findByIdForUpdate(5)).thenReturn(Optional.of(loan));

        LoanDto dto = service.renew(5);
        assertThat(dto.getDueDate()).isEqualTo(TODAY.plusDays(11 + LibraryRules.RENEWAL_DAYS).toString());
        assertError(() -> service.renew(5), ErrorCode.RENEWAL_LIMIT_REACHED);

        Loan overdue = new Loan(book, reader, librarian, TODAY.minusDays(20), TODAY.minusDays(6));
        ReflectionTestUtils.setField(overdue, "id", 6);
        when(loans.findByIdForUpdate(6)).thenReturn(Optional.of(overdue));
        assertError(() -> service.renew(6), ErrorCode.LOAN_OVERDUE_CANNOT_RENEW);
    }

    @Test
    void computesOverdueDays() {
        Loan overdue = new Loan(book, reader, librarian, TODAY.minusDays(20), TODAY.minusDays(6));
        assertThat(overdue.statusOn(TODAY)).isEqualTo(LoanStatus.OVERDUE);
        assertThat(overdue.daysOverdue(TODAY)).isEqualTo(6);

        Loan dueToday = new Loan(book, reader, librarian, TODAY.minusDays(14), TODAY);
        assertThat(dueToday.statusOn(TODAY)).isEqualTo(LoanStatus.ACTIVE);
    }

    private static void assertError(Runnable action, ErrorCode code) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(code);
    }
}
