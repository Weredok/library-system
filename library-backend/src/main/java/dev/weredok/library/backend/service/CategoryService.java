package dev.weredok.library.backend.service;

import dev.weredok.library.backend.domain.Category;
import dev.weredok.library.backend.error.BusinessException;
import dev.weredok.library.backend.error.NotFoundException;
import dev.weredok.library.backend.mapper.DtoMapper;
import dev.weredok.library.backend.repository.CategoryRepository;
import dev.weredok.library.shared.ErrorCode;
import dev.weredok.library.shared.Validation;
import dev.weredok.library.shared.dto.CategoryDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> list() {
        return categories.findAllByOrderByNameAsc().stream().map(DtoMapper::category).toList();
    }

    @Transactional
    public CategoryDto create(CategoryDto form) {
        String name = validName(form);
        if (categories.existsByNameIgnoreCase(name)) {
            throw new BusinessException(ErrorCode.CATEGORY_EXISTS);
        }
        return DtoMapper.category(categories.save(new Category(name)));
    }

    @Transactional
    public CategoryDto rename(Integer id, CategoryDto form) {
        Category c = categories.findById(id).orElseThrow(() -> new NotFoundException("Категорію", id));
        String name = validName(form);
        if (categories.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BusinessException(ErrorCode.CATEGORY_EXISTS);
        }
        c.setName(name);
        return DtoMapper.category(c);
    }

    /** Книги категорії не видаляються — у них category_id стає NULL (ON DELETE SET NULL). */
    @Transactional
    public void delete(Integer id) {
        Category c = categories.findById(id).orElseThrow(() -> new NotFoundException("Категорію", id));
        categories.delete(c);
    }

    private static String validName(CategoryDto form) {
        BusinessException.throwIfInvalid(Validation.validateCategoryName(form.getName()));
        return Validation.trimToNull(form.getName());
    }
}
