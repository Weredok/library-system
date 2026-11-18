package dev.weredok.library.shared.dto;

/**
 * Читач. Дати у форматі yyyy-MM-dd.
 */
public class ReaderDto {

    private Integer id;
    private String fullName;
    private String ticketNumber;
    private String phone;
    private String email;
    private String registeredAt;
    private int activeLoans;
    private int overdueLoans;

    public ReaderDto() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
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

    public String getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(String registeredAt) {
        this.registeredAt = registeredAt;
    }

    public int getActiveLoans() {
        return activeLoans;
    }

    public void setActiveLoans(int activeLoans) {
        this.activeLoans = activeLoans;
    }

    public int getOverdueLoans() {
        return overdueLoans;
    }

    public void setOverdueLoans(int overdueLoans) {
        this.overdueLoans = overdueLoans;
    }
}
