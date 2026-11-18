package dev.weredok.library.backend.service;

import dev.weredok.library.backend.domain.Reader;
import dev.weredok.library.backend.error.BusinessException;
import dev.weredok.library.backend.error.NotFoundException;
import dev.weredok.library.backend.mapper.DtoMapper;
import dev.weredok.library.backend.repository.LoanRepository;
import dev.weredok.library.backend.repository.ReaderRepository;
import dev.weredok.library.backend.repository.Specs;
import dev.weredok.library.shared.ErrorCode;
import dev.weredok.library.shared.Validation;
import dev.weredok.library.shared.dto.ReaderDto;
import dev.weredok.library.shared.dto.ReaderFormDto;
import dev.weredok.library.shared.dto.ReaderPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReaderService {

    private final ReaderRepository readers;
    private final LoanRepository loans;
    private final TicketNumberGenerator tickets;
    private final Clock clock;

    public ReaderService(ReaderRepository readers, LoanRepository loans, TicketNumberGenerator tickets, Clock clock) {
        this.readers = readers;
        this.loans = loans;
        this.tickets = tickets;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ReaderPage search(String q, int page, int size) {
        LocalDate today = LocalDate.now(clock);
        Page<Reader> result = readers.findAll(
                Specs.readers(Validation.trimToNull(q)),
                Paging.of(page, size, Sort.by("fullName").and(Sort.by("id"))));

        List<Integer> ids = result.getContent().stream().map(Reader::getId).toList();
        Map<Integer, long[]> counts = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] row : loans.countActiveByReaderIds(ids, today)) {
                counts.put((Integer) row[0], new long[]{
                        ((Number) row[1]).longValue(),
                        row[2] == null ? 0 : ((Number) row[2]).longValue()});
            }
        }

        ReaderPage dto = new ReaderPage();
        dto.setItems(result.getContent().stream().map(r -> {
            long[] c = counts.getOrDefault(r.getId(), new long[]{0, 0});
            return DtoMapper.reader(r, c[0], c[1]);
        }).toList());
        dto.setPage(result.getNumber());
        dto.setSize(result.getSize());
        dto.setTotal(result.getTotalElements());
        return dto;
    }

    @Transactional(readOnly = true)
    public ReaderDto get(Integer id) {
        Reader r = find(id);
        return toDto(r);
    }

    @Transactional
    public ReaderDto register(ReaderFormDto form) {
        validate(form);
        if (form.getEmail() != null && readers.existsByEmail(form.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }
        Reader r = new Reader(tickets.next(), LocalDate.now(clock));
        apply(r, form);
        readers.save(r);
        return DtoMapper.reader(r, 0, 0);
    }

    @Transactional
    public ReaderDto update(Integer id, ReaderFormDto form) {
        validate(form);
        Reader r = find(id);
        if (form.getEmail() != null && readers.existsByEmailAndIdNot(form.getEmail(), id)) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }
        apply(r, form);
        return toDto(r);
    }

    @Transactional
    public void archive(Integer id) {
        Reader r = readers.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Читача", id));
        if (r.isArchived()) {
            return;
        }
        if (loans.countActiveByReader(id) > 0) {
            throw new BusinessException(ErrorCode.READER_HAS_ACTIVE_LOANS);
        }
        r.setArchivedAt(OffsetDateTime.now(clock));
    }

    private ReaderDto toDto(Reader r) {
        LocalDate today = LocalDate.now(clock);
        long active = 0;
        long overdue = 0;
        for (Object[] row : loans.countActiveByReaderIds(List.of(r.getId()), today)) {
            active = ((Number) row[1]).longValue();
            overdue = row[2] == null ? 0 : ((Number) row[2]).longValue();
        }
        return DtoMapper.reader(r, active, overdue);
    }

    private Reader find(Integer id) {
        Reader r = readers.findById(id).orElseThrow(() -> new NotFoundException("Читача", id));
        if (r.isArchived()) {
            throw new NotFoundException("Читача", id);
        }
        return r;
    }

    private static void validate(ReaderFormDto form) {
        Validation.normalizeReader(form);
        BusinessException.throwIfInvalid(Validation.validateReader(form));
    }

    private static void apply(Reader r, ReaderFormDto form) {
        r.setFullName(form.getFullName());
        r.setPhone(form.getPhone());
        r.setEmail(form.getEmail());
    }
}
