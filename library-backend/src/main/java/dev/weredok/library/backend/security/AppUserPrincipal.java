package dev.weredok.library.backend.security;

import dev.weredok.library.backend.domain.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/** Те, що лежить у сесії після входу. Зберігаємо id, щоб записувати його у loan.issued_by / received_by. */
public class AppUserPrincipal implements UserDetails, Serializable {

    private final Integer id;
    private final String username;
    private final String password;
    private final String role;
    private final boolean enabled;

    public AppUserPrincipal(AppUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.role = user.getRole().name();
        this.enabled = user.isEnabled();
    }

    public Integer getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
