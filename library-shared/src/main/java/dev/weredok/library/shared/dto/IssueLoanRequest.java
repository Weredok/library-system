package dev.weredok.library.shared.dto;

/**
 * Запит на видачу книги. Терміни визначає сервер за LibraryRules.
 */
public class IssueLoanRequest {

    private Integer readerId;
    private Integer bookId;

    public IssueLoanRequest() {
    }

    public Integer getReaderId() {
        return readerId;
    }

    public void setReaderId(Integer readerId) {
        this.readerId = readerId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }
}
