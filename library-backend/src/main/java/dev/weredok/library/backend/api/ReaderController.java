package dev.weredok.library.backend.api;

import dev.weredok.library.backend.service.LoanService;
import dev.weredok.library.backend.service.ReaderService;
import dev.weredok.library.shared.LoanFilter;
import dev.weredok.library.shared.dto.LoanPage;
import dev.weredok.library.shared.dto.ReaderDto;
import dev.weredok.library.shared.dto.ReaderFormDto;
import dev.weredok.library.shared.dto.ReaderPage;
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
@RequestMapping("/api/readers")
public class ReaderController {

    private final ReaderService readers;
    private final LoanService loans;

    public ReaderController(ReaderService readers, LoanService loans) {
        this.readers = readers;
        this.loans = loans;
    }

    @GetMapping
    public ReaderPage search(@RequestParam(defaultValue = "") String q,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size) {
        return readers.search(q, page, size);
    }

    @GetMapping("/{id}")
    public ReaderDto get(@PathVariable Integer id) {
        return readers.get(id);
    }

    /** Історія видач читача, найновіші зверху. */
    @GetMapping("/{id}/loans")
    public LoanPage history(@PathVariable Integer id,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size) {
        readers.get(id); // 404, якщо читача немає
        return loans.search(LoanFilter.ALL, id, null, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReaderDto register(@RequestBody ReaderFormDto body) {
        return readers.register(body);
    }

    @PutMapping("/{id}")
    public ReaderDto update(@PathVariable Integer id, @RequestBody ReaderFormDto body) {
        return readers.update(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void archive(@PathVariable Integer id) {
        readers.archive(id);
    }
}
