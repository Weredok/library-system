package dev.weredok.library.backend.service;

import dev.weredok.library.backend.domain.AppUser;
import dev.weredok.library.backend.error.BusinessException;
import dev.weredok.library.backend.repository.AppUserRepository;
import dev.weredok.library.backend.repository.BookRepository;
import dev.weredok.library.backend.repository.LoanRepository;
import dev.weredok.library.backend.repository.ReaderRepository;
import dev.weredok.library.shared.ErrorCode;
import dev.weredok.library.shared.dto.BookDto;
import dev.weredok.library.shared.dto.BookFormDto;
import dev.weredok.library.shared.dto.IssueLoanRequest;
import dev.weredok.library.shared.dto.ReaderDto;
import dev.weredok.library.shared.dto.ReaderFormDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Інтеграційний тест на РЕАЛЬНІЙ базі (та, що в DB_URL). Запуск: mvn verify -Pit
 * Перевіряє головне: два бібліотекарі одночасно видають останній примірник — пройти має рівно одна видача.
 * Тест створює свої книгу й читачів і прибирає за собою.
 */
@SpringBootTest
class LoanConcurrencyIT {

    @Autowired
    private LoanService loanService;
    @Autowired
    private BookService bookService;
    @Autowired
    private ReaderService readerService;
    @Autowired
    private LoanRepository loans;
    @Autowired
    private BookRepository books;
    @Autowired
    private ReaderRepository readers;
    @Autowired
    private AppUserRepository users;

    private final List<Integer> createdReaders = new ArrayList<>();
    private Integer createdBook;

    @Test
    void onlyOneOfConcurrentIssuesGetsTheLastCopy() throws Exception {
        BookFormDto bookForm = new BookFormDto();
        bookForm.setTitle("IT: останній примірник " + System.nanoTime());
        bookForm.setAuthor("Тест");
        bookForm.setTotalCopies(1);
        BookDto book = bookService.create(bookForm);
        createdBook = book.getId();

        int threads = 8;
        List<Integer> readerIds = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            ReaderFormDto f = new ReaderFormDto();
            f.setFullName("IT Читач " + i);
            ReaderDto r = readerService.register(f);
            readerIds.add(r.getId());
            createdReaders.add(r.getId());
        }
        AppUser librarian = users.findByUsername("librarian").orElseThrow();

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger noCopies = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();
        for (Integer readerId : readerIds) {
            futures.add(pool.submit(() -> {
                IssueLoanRequest req = new IssueLoanRequest();
                req.setReaderId(readerId);
                req.setBookId(book.getId());
                start.await();
                try {
                    loanService.issue(req, librarian.getId());
                    success.incrementAndGet();
                } catch (BusinessException e) {
                    if (e.getCode() == ErrorCode.NO_COPIES_AVAILABLE) {
                        noCopies.incrementAndGet();
                    } else {
                        throw e;
                    }
                }
                return null;
            }));
        }
        start.countDown();
        for (Future<?> f : futures) {
            f.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();

        assertThat(success.get()).isEqualTo(1);
        assertThat(noCopies.get()).isEqualTo(threads - 1);
        assertThat(loans.countActiveByBook(book.getId())).isEqualTo(1);
    }

    @AfterEach
    void cleanUp() {
        // Тестові дані видаляємо напряму — у робочому коді видалення історії немає
        loans.findAll().stream()
                .filter(l -> l.getBook().getId().equals(createdBook))
                .forEach(loans::delete);
        createdReaders.forEach(readers::deleteById);
        if (createdBook != null) {
            books.deleteById(createdBook);
        }
    }
}
