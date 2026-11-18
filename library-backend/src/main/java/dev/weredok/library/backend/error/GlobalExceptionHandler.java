package dev.weredok.library.backend.error;

import dev.weredok.library.shared.ErrorCode;
import dev.weredok.library.shared.dto.ErrorDto;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;

/** Усі помилки API повертаються як {code, message, details} — клієнт показує message як є. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Назви обмежень з V1__schema.sql → зрозумілі коди помилок. */
    private static final Map<String, ErrorCode> CONSTRAINTS = Map.of(
            "uq_book_isbn", ErrorCode.ISBN_EXISTS,
            "uq_reader_email", ErrorCode.EMAIL_EXISTS,
            "uq_reader_ticket", ErrorCode.TICKET_EXISTS,
            "uq_category_name", ErrorCode.CATEGORY_EXISTS,
            "ck_book_total_copies", ErrorCode.VALIDATION_ERROR,
            "fk_loan_book", ErrorCode.BOOK_HAS_ACTIVE_LOANS,
            "fk_loan_reader", ErrorCode.READER_HAS_ACTIVE_LOANS);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDto> business(BusinessException e) {
        return respond(statusOf(e.getCode()), e.getCode(), e.getMessage(), e.getDetails());
    }

    /**
     * Сервіс перевіряє унікальність заздалегідь, але між перевіркою і INSERT
     * хтось міг встигнути — тоді спрацьовує обмеження БД, і ми потрапляємо сюди.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDto> integrity(DataIntegrityViolationException e) {
        String constraint = null;
        if (e.getCause() instanceof ConstraintViolationException cve) {
            constraint = cve.getConstraintName();
        }
        ErrorCode code = constraint == null ? null : CONSTRAINTS.get(constraint.toLowerCase());
        if (code == null) {
            log.warn("Порушення обмеження БД: {}", constraint, e);
            code = ErrorCode.DATA_CONFLICT;
        }
        return respond(statusOf(code), code, code.getDefaultMessage(), List.of());
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ErrorDto> lock(PessimisticLockingFailureException e) {
        return respond(HttpStatus.CONFLICT, ErrorCode.DATA_CONFLICT, "Запис зараз змінює інший користувач, спробуйте ще раз", List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> denied(AccessDeniedException e) {
        return respond(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getDefaultMessage(), List.of());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorDto> badRequest(Exception e) {
        return respond(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Некоректний запит", List.of(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> unexpected(Exception e) {
        log.error("Неочікувана помилка", e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage(), List.of());
    }

    public static HttpStatus statusOf(ErrorCode code) {
        return switch (code) {
            case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case UNAUTHORIZED, BAD_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.CONFLICT;
        };
    }

    public static ErrorDto dto(ErrorCode code, String message, List<String> details) {
        ErrorDto dto = new ErrorDto();
        dto.setCode(code.name());
        dto.setMessage(message);
        dto.setDetails(details);
        return dto;
    }

    private static ResponseEntity<ErrorDto> respond(HttpStatus status, ErrorCode code, String message, List<String> details) {
        return ResponseEntity.status(status).body(dto(code, message, details));
    }
}
