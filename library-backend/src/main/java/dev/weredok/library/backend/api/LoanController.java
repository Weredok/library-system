package dev.weredok.library.backend.api;

import dev.weredok.library.backend.security.AppUserPrincipal;
import dev.weredok.library.backend.service.LoanService;
import dev.weredok.library.shared.LoanFilter;
import dev.weredok.library.shared.dto.IssueLoanRequest;
import dev.weredok.library.shared.dto.LoanDto;
import dev.weredok.library.shared.dto.LoanPage;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService service;

    public LoanController(LoanService service) {
        this.service = service;
    }

    /** readerId / bookId = 0 — без фільтру. */
    @GetMapping
    public LoanPage search(@RequestParam(defaultValue = "ALL") LoanFilter filter,
                           @RequestParam(defaultValue = "0") int readerId,
                           @RequestParam(defaultValue = "0") int bookId,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size) {
        return service.search(filter, readerId > 0 ? readerId : null, bookId > 0 ? bookId : null, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanDto issue(@RequestBody IssueLoanRequest body, @AuthenticationPrincipal AppUserPrincipal me) {
        return service.issue(body, me.getId());
    }

    @PostMapping("/{id}/return")
    public LoanDto returnBook(@PathVariable Integer id, @AuthenticationPrincipal AppUserPrincipal me) {
        return service.returnBook(id, me.getId());
    }

    @PostMapping("/{id}/renew")
    public LoanDto renew(@PathVariable Integer id) {
        return service.renew(id);
    }
}
