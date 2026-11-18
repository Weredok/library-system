package dev.weredok.library.backend.error;

import dev.weredok.library.shared.ErrorCode;

public class NotFoundException extends BusinessException {

    public NotFoundException(String what, Object id) {
        super(ErrorCode.NOT_FOUND, what + " з id " + id + " не знайдено");
    }
}
