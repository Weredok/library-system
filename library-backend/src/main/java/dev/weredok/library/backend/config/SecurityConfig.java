package dev.weredok.library.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.weredok.library.backend.error.GlobalExceptionHandler;
import dev.weredok.library.shared.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import dev.weredok.library.backend.security.AppUserDetailsService;

import java.io.IOException;
import java.util.List;

/**
 * Сесійна автентифікація для SPA:
 * <ul>
 *     <li>POST /api/auth/login з JSON — сервер кладе SecurityContext у сесію (cookie JSESSIONID);</li>
 *     <li>усі /api/** крім логіну вимагають входу, без нього — JSON 401, а не редірект на форму;</li>
 *     <li>CSRF-токени вимкнені, натомість cookie має SameSite=Strict (див. application.yml).
 *     Для навчального проєкту цього достатньо; для продакшну варто ввімкнути CSRF-токени.</li>
 * </ul>
 * Ролі: ADMIN може все, LIBRARIAN — все, крім керування категоріями і списання (архівування).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper mapper) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .securityContext(sc -> sc.securityContextRepository(securityContextRepository()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                writeError(res, mapper, ErrorCode.UNAUTHORIZED))
                        .accessDeniedHandler((req, res, e) ->
                                writeError(res, mapper, ErrorCode.FORBIDDEN)));
        return http.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /** Підтримує {bcrypt}, {noop} та інші префікси; нові паролі хешує bcrypt. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AppUserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setUserDetailsPasswordService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    private static void writeError(HttpServletResponse res, ObjectMapper mapper, ErrorCode code) throws IOException {
        res.setStatus(GlobalExceptionHandler.statusOf(code).value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getOutputStream(), GlobalExceptionHandler.dto(code, code.getDefaultMessage(), List.of()));
    }
}
