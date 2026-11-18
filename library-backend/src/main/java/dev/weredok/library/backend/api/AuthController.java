package dev.weredok.library.backend.api;

import dev.weredok.library.backend.error.BusinessException;
import dev.weredok.library.backend.security.AppUserPrincipal;
import dev.weredok.library.shared.ErrorCode;
import dev.weredok.library.shared.dto.CurrentUserDto;
import dev.weredok.library.shared.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextRepository;

    public AuthController(AuthenticationManager authenticationManager, SecurityContextRepository contextRepository) {
        this.authenticationManager = authenticationManager;
        this.contextRepository = contextRepository;
    }

    @PostMapping("/login")
    public CurrentUserDto login(@RequestBody LoginRequest body, HttpServletRequest request, HttpServletResponse response) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(body.getUsername(), body.getPassword()));
        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS);
        }

        // Захист від session fixation: після входу сесія отримує новий id
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);

        return toDto((AppUserPrincipal) auth.getPrincipal());
    }

    @GetMapping("/me")
    public CurrentUserDto me(@AuthenticationPrincipal AppUserPrincipal me) {
        return toDto(me);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    private static CurrentUserDto toDto(AppUserPrincipal p) {
        CurrentUserDto dto = new CurrentUserDto();
        dto.setId(p.getId());
        dto.setUsername(p.getUsername());
        dto.setRole(p.getRole());
        return dto;
    }
}
