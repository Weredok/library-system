package dev.weredok.library.shared.dto;

/**
 * Користувач, який зараз увійшов. role: ADMIN або LIBRARIAN.
 */
public class CurrentUserDto {

    private Integer id;
    private String username;
    private String role;

    public CurrentUserDto() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
