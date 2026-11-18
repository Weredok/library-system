package dev.weredok.library.shared.dto;

import java.util.List;

/**
 * Тіло відповіді з помилкою. code — назва з ErrorCode, details — список проблем валідації.
 */
public class ErrorDto {

    private String code;
    private String message;
    private List<String> details;

    public ErrorDto() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }
}
