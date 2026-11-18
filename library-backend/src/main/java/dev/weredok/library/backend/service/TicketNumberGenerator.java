package dev.weredok.library.backend.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Номер квитка: R-2026-000123. Число береться з послідовності БД — вона атомарна,
 * тож два одночасні запити ніколи не отримають однаковий номер.
 */
@Component
public class TicketNumberGenerator {

    private final EntityManager em;
    private final Clock clock;

    public TicketNumberGenerator(EntityManager em, Clock clock) {
        this.em = em;
        this.clock = clock;
    }

    public String next() {
        Number n = (Number) em.createNativeQuery("select nextval('reader_ticket_seq')").getSingleResult();
        return String.format("R-%d-%06d", LocalDate.now(clock).getYear(), n.longValue());
    }
}
