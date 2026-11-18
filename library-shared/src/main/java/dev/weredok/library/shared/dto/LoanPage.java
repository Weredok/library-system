package dev.weredok.library.shared.dto;

import java.util.List;

/**
 * Сторінка видач.
 */
public class LoanPage {

    private List<LoanDto> items;
    private int page;
    private int size;
    private long total;

    public LoanPage() {
    }

    public List<LoanDto> getItems() {
        return items;
    }

    public void setItems(List<LoanDto> items) {
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
