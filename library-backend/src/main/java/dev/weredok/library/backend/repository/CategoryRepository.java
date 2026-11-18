package dev.weredok.library.backend.repository;

import dev.weredok.library.backend.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}
