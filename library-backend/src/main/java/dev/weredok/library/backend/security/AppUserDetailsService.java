package dev.weredok.library.backend.security;

import dev.weredok.library.backend.domain.AppUser;
import dev.weredok.library.backend.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

    private static final Logger log = LoggerFactory.getLogger(AppUserDetailsService.class);

    private final AppUserRepository users;

    public AppUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        return users.findByUsername(username)
                .map(AppUserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    /**
     * Spring Security викликає це після успішного входу, якщо пароль збережено застарілим способом
     * (наприклад, {noop} з тестових даних) — і ми перезаписуємо його bcrypt-хешем.
     */
    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        AppUser appUser = users.findByUsername(user.getUsername()).orElseThrow();
        appUser.setPasswordHash(newPassword);
        log.info("Пароль користувача {} перехешовано", user.getUsername());
        return new AppUserPrincipal(appUser);
    }
}
