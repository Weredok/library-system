package dev.weredok.library.backend.repository;

import dev.weredok.library.backend.domain.Book;
import dev.weredok.library.backend.domain.Loan;
import dev.weredok.library.backend.domain.Reader;
import dev.weredok.library.shared.LoanFilter;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Динамічні фільтри для списків (Criteria API). */
public final class Specs {

    private Specs() {
    }

    /**
     * @param q             підрядок у назві, авторі або ISBN (null — без фільтру)
     * @param categoryId    категорія (null — усі)
     * @param availableOnly тільки ті, де є вільний примірник
     */
    public static Specification<Book> books(String q, Integer categoryId, boolean availableOnly) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.isNull(root.get("archivedAt")));
            if (q != null) {
                String like = likePattern(q);
                p.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("author")), like),
                        cb.like(root.get("isbn"), like.replace("-", ""))));
            }
            if (categoryId != null) {
                p.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (availableOnly) {
                Subquery<Long> active = query.subquery(Long.class);
                Root<Loan> l = active.from(Loan.class);
                active.select(cb.count(l))
                        .where(cb.equal(l.get("book"), root), cb.isNull(l.get("returnDate")));
                p.add(cb.gt(root.<Integer>get("totalCopies"), active));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    public static Specification<Reader> readers(String q) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.isNull(root.get("archivedAt")));
            if (q != null) {
                String like = likePattern(q);
                p.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), like),
                        cb.like(cb.lower(root.get("ticketNumber")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(root.get("phone"), like)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    public static Specification<Loan> loans(LoanFilter filter, Integer readerId, Integer bookId, LocalDate today) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (readerId != null) {
                p.add(cb.equal(root.get("reader").get("id"), readerId));
            }
            if (bookId != null) {
                p.add(cb.equal(root.get("book").get("id"), bookId));
            }
            switch (filter == null ? LoanFilter.ALL : filter) {
                case ON_HAND -> p.add(cb.isNull(root.get("returnDate")));
                case OVERDUE -> {
                    p.add(cb.isNull(root.get("returnDate")));
                    p.add(cb.lessThan(root.get("dueDate"), today));
                }
                case RETURNED -> p.add(cb.isNotNull(root.get("returnDate")));
                case ALL -> {
                }
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    /** Екранує спецсимволи LIKE, щоб пошук «50%» не знаходив усе підряд. */
    private static String likePattern(String q) {
        String escaped = q.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
