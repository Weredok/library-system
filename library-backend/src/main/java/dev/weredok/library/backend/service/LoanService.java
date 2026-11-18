package dev.weredok.library.backend.service;

import dev.weredok.library.backend.domain.AppUser;
import dev.weredok.library.backend.domain.Book;
import dev.weredok.library.backend.domain.Loan;
import dev.weredok.library.backend.domain.Reader;
import dev.weredok.library.backend.error.BusinessException;
import dev.weredok.library.backend.error.NotFoundException;
import dev.weredok.library.backend.mapper.DtoMapper;
import dev.weredok.library.backend.repository.AppUserRepository;
import dev.weredok.library.backend.repository.BookRepository;
import dev.weredok.library.backend.repository.LoanRepository;
import dev.weredok.library.backend.repository.ReaderRepository;
import dev.weredok.library.backend.repository.Specs;
import dev.weredok.library.shared.ErrorCode;
import dev.weredok.library.shared.LibraryRules;
import dev.weredok.library.shared.LoanFilter;
import dev.weredok.library.shared.dto.IssueLoanRequest;
import dev.weredok.library.shared.dto.LoanDto;
import dev.weredok.library.shared.dto.LoanPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Абонемент: видача, повернення, продовження.
 * <p>
 * Конкурентність: при видачі блокуємо рядки читача і книги (SELECT ... FOR UPDATE) у фіксованому
 * порядку «спочатку читач, потім книга». Поки транзакція не завершилась, інша видача тієї ж книги
 * або тому ж читачу чекає — тому перевірки «є вільний примірник» і «не перевищено ліміт» не можуть
 * пройти одночасно у двох транзакціях.
 */
@Service
public class LoanService {

    private final LoanRepository loans;
    private final BookRepository books;
    private final ReaderRepository readers;
    private final AppUserRepository users;
    private final Clock clock;

    public LoanService(LoanRepository loans, BookRepository books, ReaderRepository readers,
                       AppUserRepository users, Clock clock) {
        this.loans = loans;
        this.books = books;
        this.readers = readers;
        this.users = users;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public LoanPage search(LoanFilter filter, Integer readerId, Integer bookId, int page, int size) {
        LocalDate today = today();
        Page<Loan> result = loans.findAll(
                Specs.loans(filter, readerId, bookId, today),
                Paging.of(page, size, Sort.by(Sort.Direction.DESC, "issueDate").and(Sort.by(Sort.Direction.DESC, "id"))));
        LoanPage dto = new LoanPage();
        dto.setItems(result.getContent().stream().map(l -> DtoMapper.loan(l, today)).toList());
        dto.setPage(result.getNumber());
        dto.setSize(result.getSize());
        dto.setTotal(result.getTotalElements());
        return dto;
    }

    @Transactional
    public LoanDto issue(IssueLoanRequest request, Integer librarianId) {
        if (request.getReaderId() == null || request.getBookId() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Потрібно вибрати читача і книгу");
        }
        LocalDate today = today();

        Reader reader = readers.findByIdForUpdate(request.getReaderId())
                .orElseThrow(() -> new NotFoundException("Читача", request.getReaderId()));
        if (reader.isArchived()) {
            throw new BusinessException(ErrorCode.READER_ARCHIVED);
        }

        Book book = books.findByIdForUpdate(request.getBookId())
                .orElseThrow(() -> new NotFoundException("Книгу", request.getBookId()));
        if (book.isArchived()) {
            throw new BusinessException(ErrorCode.BOOK_ARCHIVED);
        }

        if (loans.hasOverdue(reader.getId(), today)) {
            throw new BusinessException(ErrorCode.READER_HAS_OVERDUE);
        }
        if (loans.countActiveByReader(reader.getId()) >= LibraryRules.MAX_ACTIVE_LOANS_PER_READER) {
            throw new BusinessException(ErrorCode.LOAN_LIMIT_REACHED,
                    ErrorCode.LOAN_LIMIT_REACHED.getDefaultMessage()
                            + " (" + LibraryRules.MAX_ACTIVE_LOANS_PER_READER + ")");
        }
        if (loans.existsActive(reader.getId(), book.getId())) {
            throw new BusinessException(ErrorCode.BOOK_ALREADY_ON_HAND);
        }
        if (loans.countActiveByBook(book.getId()) >= book.getTotalCopies()) {
            throw new BusinessException(ErrorCode.NO_COPIES_AVAILABLE);
        }

        Loan loan = new Loan(book, reader, librarian(librarianId), today, today.plusDays(LibraryRules.LOAN_DAYS));
        loans.save(loan);
        return DtoMapper.loan(loan, today);
    }

    @Transactional
    public LoanDto returnBook(Integer loanId, Integer librarianId) {
        Loan loan = lockLoan(loanId);
        if (loan.isReturned()) {
            throw new BusinessException(ErrorCode.LOAN_ALREADY_RETURNED);
        }
        LocalDate today = today();
        loan.markReturned(today, librarian(librarianId));
        return DtoMapper.loan(loan, today);
    }

    @Transactional
    public LoanDto renew(Integer loanId) {
        Loan loan = lockLoan(loanId);
        LocalDate today = today();
        if (loan.isReturned()) {
            throw new BusinessException(ErrorCode.LOAN_ALREADY_RETURNED);
        }
        if (loan.isOverdue(today)) {
            throw new BusinessException(ErrorCode.LOAN_OVERDUE_CANNOT_RENEW);
        }
        if (loan.getRenewalCount() >= LibraryRules.MAX_RENEWALS) {
            throw new BusinessException(ErrorCode.RENEWAL_LIMIT_REACHED);
        }
        loan.renew(LibraryRules.RENEWAL_DAYS);
        return DtoMapper.loan(loan, today);
    }

    private Loan lockLoan(Integer loanId) {
        return loans.findByIdForUpdate(loanId).orElseThrow(() -> new NotFoundException("Видачу", loanId));
    }

    private AppUser librarian(Integer id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("Користувача", id));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
