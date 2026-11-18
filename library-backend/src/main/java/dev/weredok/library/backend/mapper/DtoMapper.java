package dev.weredok.library.backend.mapper;

import dev.weredok.library.backend.domain.AppUser;
import dev.weredok.library.backend.domain.Book;
import dev.weredok.library.backend.domain.Category;
import dev.weredok.library.backend.domain.Loan;
import dev.weredok.library.backend.domain.Reader;
import dev.weredok.library.shared.LibraryRules;
import dev.weredok.library.shared.LoanStatus;
import dev.weredok.library.shared.dto.BookDto;
import dev.weredok.library.shared.dto.CategoryDto;
import dev.weredok.library.shared.dto.LoanDto;
import dev.weredok.library.shared.dto.OverdueDto;
import dev.weredok.library.shared.dto.ReaderDto;

import java.time.LocalDate;

/** Сутності → DTO. Викликається всередині транзакції, бо зв'язки LAZY. */
public final class DtoMapper {

    private DtoMapper() {
    }

    public static CategoryDto category(Category c) {
        CategoryDto dto = new CategoryDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        return dto;
    }

    public static BookDto book(Book b, long activeLoans) {
        BookDto dto = new BookDto();
        dto.setId(b.getId());
        dto.setTitle(b.getTitle());
        dto.setAuthor(b.getAuthor());
        dto.setIsbn(b.getIsbn());
        dto.setYear(b.getYear());
        if (b.getCategory() != null) {
            dto.setCategoryId(b.getCategory().getId());
            dto.setCategoryName(b.getCategory().getName());
        }
        dto.setTotalCopies(b.getTotalCopies());
        dto.setAvailableCopies((int) Math.max(0, b.getTotalCopies() - activeLoans));
        return dto;
    }

    public static ReaderDto reader(Reader r, long activeLoans, long overdueLoans) {
        ReaderDto dto = new ReaderDto();
        dto.setId(r.getId());
        dto.setFullName(r.getFullName());
        dto.setTicketNumber(r.getTicketNumber());
        dto.setPhone(r.getPhone());
        dto.setEmail(r.getEmail());
        dto.setRegisteredAt(date(r.getRegisteredAt()));
        dto.setActiveLoans((int) activeLoans);
        dto.setOverdueLoans((int) overdueLoans);
        return dto;
    }

    public static LoanDto loan(Loan l, LocalDate today) {
        LoanDto dto = new LoanDto();
        dto.setId(l.getId());
        dto.setBookId(l.getBook().getId());
        dto.setBookTitle(l.getBook().getTitle());
        dto.setBookAuthor(l.getBook().getAuthor());
        dto.setReaderId(l.getReader().getId());
        dto.setReaderName(l.getReader().getFullName());
        dto.setTicketNumber(l.getReader().getTicketNumber());
        dto.setIssueDate(date(l.getIssueDate()));
        dto.setDueDate(date(l.getDueDate()));
        dto.setReturnDate(date(l.getReturnDate()));
        dto.setRenewalCount(l.getRenewalCount());
        LoanStatus status = l.statusOn(today);
        dto.setStatus(status);
        dto.setDaysOverdue(l.daysOverdue(today));
        dto.setIssuedBy(username(l.getIssuedBy()));
        dto.setReceivedBy(username(l.getReceivedBy()));
        dto.setCanRenew(status == LoanStatus.ACTIVE && l.getRenewalCount() < LibraryRules.MAX_RENEWALS);
        return dto;
    }

    public static OverdueDto overdue(Loan l, LocalDate today) {
        OverdueDto dto = new OverdueDto();
        dto.setLoanId(l.getId());
        dto.setReaderId(l.getReader().getId());
        dto.setReaderName(l.getReader().getFullName());
        dto.setTicketNumber(l.getReader().getTicketNumber());
        dto.setPhone(l.getReader().getPhone());
        dto.setEmail(l.getReader().getEmail());
        dto.setBookId(l.getBook().getId());
        dto.setBookTitle(l.getBook().getTitle());
        dto.setIssueDate(date(l.getIssueDate()));
        dto.setDueDate(date(l.getDueDate()));
        dto.setDaysOverdue(l.daysOverdue(today));
        return dto;
    }

    private static String date(LocalDate d) {
        return d == null ? null : d.toString();
    }

    private static String username(AppUser u) {
        return u == null ? null : u.getUsername();
    }
}
