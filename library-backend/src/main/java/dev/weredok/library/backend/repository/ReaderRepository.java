package dev.weredok.library.backend.repository;

import dev.weredok.library.backend.domain.Reader;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReaderRepository extends JpaRepository<Reader, Integer>, JpaSpecificationExecutor<Reader> {

    /** Блокування читача при видачі — щоб дві одночасні видачі не обійшли ліміт книг. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reader r where r.id = :id")
    Optional<Reader> findByIdForUpdate(@Param("id") Integer id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Integer id);
}
