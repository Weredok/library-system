package dev.weredok.library.backend.service;

import dev.weredok.library.shared.LibraryRules;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

final class Paging {

    private Paging() {
    }

    /** page рахується з 0. Розмір обмежуємо, щоб ніхто не попросив мільйон рядків одним запитом. */
    static PageRequest of(int page, int size, Sort sort) {
        int p = Math.max(0, page);
        int s = size <= 0 ? 20 : Math.min(size, LibraryRules.MAX_PAGE_SIZE);
        return PageRequest.of(p, s, sort);
    }
}
