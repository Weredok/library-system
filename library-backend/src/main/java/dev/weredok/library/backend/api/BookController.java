package dev.weredok.library.backend.api;

import dev.weredok.library.backend.service.BookService;
import dev.weredok.library.shared.dto.BookDto;
import dev.weredok.library.shared.dto.BookFormDto;
import dev.weredok.library.shared.dto.BookPage;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    /** categoryId = 0 або відсутній — усі категорії. page рахується з 0. */
    @GetMapping
    public BookPage search(@RequestParam(defaultValue = "") String q,
                           @RequestParam(defaultValue = "0") int categoryId,
                           @RequestParam(defaultValue = "false") boolean availableOnly,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size) {
        return service.search(q, categoryId > 0 ? categoryId : null, availableOnly, page, size);
    }

    @GetMapping("/{id}")
    public BookDto get(@PathVariable Integer id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto create(@RequestBody BookFormDto body) {
        return service.create(body);
    }

    @PutMapping("/{id}")
    public BookDto update(@PathVariable Integer id, @RequestBody BookFormDto body) {
        return service.update(id, body);
    }

    /** Списання книги (м'яке видалення). */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void archive(@PathVariable Integer id) {
        service.archive(id);
    }
}
