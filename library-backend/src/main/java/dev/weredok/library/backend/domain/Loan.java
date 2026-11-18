package dev.weredok.library.backend.domain;

import dev.weredok.library.shared.LoanStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Запис журналу видач. Після повернення запис не видаляється і не редагується —
 * це і є історія «хто, що, коли взяв і хто прийняв назад».
 */
@Entity
@Table(name = "loan")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false, updatable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reader_id", nullable = false, updatable = false)
    private Reader reader;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issued_by", nullable = false, updatable = false)
    private AppUser issuedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by")
    private AppUser receivedBy;

    @Column(name = "issue_date", nullable = false, updatable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "renewal_count", nullable = false)
    private int renewalCount;

    protected Loan() {
    }

    public Loan(Book book, Reader reader, AppUser issuedBy, LocalDate issueDate, LocalDate dueDate) {
        this.book = book;
        this.reader = reader;
        this.issuedBy = issuedBy;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    public boolean isOverdue(LocalDate today) {
        return returnDate == null && dueDate.isBefore(today);
    }

    public LoanStatus statusOn(LocalDate today) {
        if (returnDate != null) {
            return LoanStatus.RETURNED;
        }
        return isOverdue(today) ? LoanStatus.OVERDUE : LoanStatus.ACTIVE;
    }

    /** Скільки днів прострочено на дату today (0, якщо не прострочено). */
    public int daysOverdue(LocalDate today) {
        if (!isOverdue(today)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(dueDate, today);
    }

    public void markReturned(LocalDate date, AppUser receiver) {
        this.returnDate = date;
        this.receivedBy = receiver;
    }

    public void renew(int days) {
        this.dueDate = dueDate.plusDays(days);
        this.renewalCount++;
    }

    public Integer getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public Reader getReader() {
        return reader;
    }

    public AppUser getIssuedBy() {
        return issuedBy;
    }

    public AppUser getReceivedBy() {
        return receivedBy;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public int getRenewalCount() {
        return renewalCount;
    }
}
