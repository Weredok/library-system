package dev.weredok.library.shared.dto;

import java.util.List;

/**
 * Сторінка книг.
 */
public class BookPage {

    private List<BookDto> items;
    private int page;
    private int size;
    private long total;

    public BookPage() {
    }

    public List<BookDto> getItems() {
        return items;
    }

    public void setItems(List<BookDto> items) {
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
