package dev.weredok.library.backend.error;

import dev.weredok.library.shared.ErrorCode;

import java.util.List;

/** Порушення бізнес-правила. Перетворюється на JSON ErrorDto у GlobalExceptionHandler. */
public class BusinessException extends RuntimeException {

    private final ErrorCode code;
    private final List<String> details;

    public BusinessException(ErrorCode code) {
        this(code, code.getDefaultMessage(), List.of());
    }

    public BusinessException(ErrorCode code, String message) {
        this(code, message, List.of());
    }

    public BusinessException(ErrorCode code, String message, List<String> details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public static void throwIfInvalid(List<String> errors) {
        if (!errors.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.getDefaultMessage(), errors);
        }
    }

    public ErrorCode getCode() {
        return code;
    }

    public List<String> getDetails() {
        return details;
    }
}
