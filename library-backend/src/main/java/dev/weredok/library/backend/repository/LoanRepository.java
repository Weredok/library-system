package dev.weredok.library.backend.repository;

import dev.weredok.library.backend.domain.Loan;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Integer>, JpaSpecificationExecutor<Loan> {

    @Override
    @EntityGraph(attributePaths = {"book", "reader", "issuedBy", "receivedBy"})
    Page<Loan> findAll(Specification<Loan> spec, Pageable pageable);

    /** Без join fetch: Postgres не дозволяє FOR UPDATE на nullable-боці outer join. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Loan l where l.id = :id")
    Optional<Loan> findByIdForUpdate(@Param("id") Integer id);

    @Query("select count(l) from Loan l where l.book.id = :bookId and l.returnDate is null")
    long countActiveByBook(@Param("bookId") Integer bookId);

    @Query("select count(l) from Loan l where l.reader.id = :readerId and l.returnDate is null")
    long countActiveByReader(@Param("readerId") Integer readerId);

    @Query("""
            select count(l) > 0 from Loan l
            where l.reader.id = :readerId and l.returnDate is null and l.dueDate < :today
            """)
    boolean hasOverdue(@Param("readerId") Integer readerId, @Param("today") LocalDate today);

    @Query("""
            select count(l) > 0 from Loan l
            where l.reader.id = :readerId and l.book.id = :bookId and l.returnDate is null
            """)
    boolean existsActive(@Param("readerId") Integer readerId, @Param("bookId") Integer bookId);

    /** [bookId, кількість активних видач] — для підрахунку вільних примірників на сторінці списку. */
    @Query("""
            select l.book.id, count(l) from Loan l
            where l.book.id in :ids and l.returnDate is null
            group by l.book.id
            """)
    List<Object[]> countActiveByBookIds(@Param("ids") Collection<Integer> ids);

    /** [readerId, на руках, з них прострочено]. */
    @Query("""
            select l.reader.id, count(l), sum(case when l.dueDate < :today then 1 else 0 end)
            from Loan l
            where l.reader.id in :ids and l.returnDate is null
            group by l.reader.id
            """)
    List<Object[]> countActiveByReaderIds(@Param("ids") Collection<Integer> ids, @Param("today") LocalDate today);

    @Query("""
            select l from Loan l
            join fetch l.reader
            join fetch l.book
            where l.returnDate is null and l.dueDate < :today
            order by l.dueDate asc, l.id asc
            """)
    List<Loan> findOverdue(@Param("today") LocalDate today);
}
