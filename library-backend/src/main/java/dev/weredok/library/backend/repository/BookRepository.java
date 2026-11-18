package dev.weredok.library.backend.repository;

import dev.weredok.library.backend.domain.Book;
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

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {

    /**
     * SELECT ... FOR UPDATE. Тримає блокування рядка книги до кінця транзакції —
     * так дві одночасні видачі останнього примірника виконуються по черзі, а не разом.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Integer id);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Integer id);

    @Override
    @EntityGraph(attributePaths = "category")
    Page<Book> findAll(Specification<Book> spec, Pageable pageable);
}
