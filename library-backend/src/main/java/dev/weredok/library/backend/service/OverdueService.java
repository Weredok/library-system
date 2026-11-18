package dev.weredok.library.backend.service;

import dev.weredok.library.backend.mapper.DtoMapper;
import dev.weredok.library.backend.repository.LoanRepository;
import dev.weredok.library.shared.dto.OverdueDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Боржники. Нічого не зберігає і не потребує cron: прострочка — це просто
 * «не повернено і due_date &lt; сьогодні», рахується в момент запиту.
 */
@Service
public class OverdueService {

    private final LoanRepository loans;
    private final Clock clock;

    public OverdueService(LoanRepository loans, Clock clock) {
        this.loans = loans;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<OverdueDto> list() {
        LocalDate today = LocalDate.now(clock);
        return loans.findOverdue(today).stream().map(l -> DtoMapper.overdue(l, today)).toList();
    }
}
