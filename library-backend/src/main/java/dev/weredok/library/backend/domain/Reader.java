package dev.weredok.library.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reader")
public class Reader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    /** Генерується при реєстрації і більше ніколи не змінюється. */
    @Column(name = "ticket_number", nullable = false, updatable = false)
    private String ticketNumber;

    private String phone;

    private String email;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDate registeredAt;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

    protected Reader() {
    }

    public Reader(String ticketNumber, LocalDate registeredAt) {
        this.ticketNumber = ticketNumber;
        this.registeredAt = registeredAt;
    }

    public boolean isArchived() {
        return archivedAt != null;
    }

    public Integer getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getRegisteredAt() {
        return registeredAt;
    }

    public OffsetDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(OffsetDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }
}
