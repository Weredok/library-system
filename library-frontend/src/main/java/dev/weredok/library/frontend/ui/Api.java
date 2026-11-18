package dev.weredok.library.frontend.ui;

import elemental2.core.Global;
import elemental2.core.JsArray;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import org.dominokit.rest.shared.request.FailedResponseBean;
import org.dominokit.rest.shared.request.ServerRequest;

import java.util.function.Consumer;

/**
 * Єдина точка відправки REST-запитів: успіх → callback, помилка → показуємо message з ErrorDto.
 * 401 → повертаємо на екран входу.
 */
public final class Api {

    private static Runnable onUnauthorized = () -> {
    };

    private Api() {
    }

    public static void setOnUnauthorized(Runnable handler) {
        onUnauthorized = handler;
    }

    public static <T> void send(ServerRequest<?, T> request, Consumer<T> onSuccess) {
        send(request, onSuccess, null);
    }

    /** onError викликається після показу повідомлення — наприклад, щоб розблокувати кнопку. */
    public static <T> void send(ServerRequest<?, T> request, Consumer<T> onSuccess, Runnable onError) {
        request
                .onSuccess(onSuccess::accept)
                .onFailed(failed -> {
                    handleFailure(failed);
                    if (onError != null) {
                        onError.run();
                    }
                })
                .send();
    }

    private static void handleFailure(FailedResponseBean failed) {
        // 401 з кодом UNAUTHORIZED — сесії немає або вона закінчилась → на екран входу.
        // 401 з BAD_CREDENTIALS — невірний пароль, показуємо повідомлення.
        if (failed.getStatusCode() == 401 && !"BAD_CREDENTIALS".equals(codeOf(failed))) {
            onUnauthorized.run();
            return;
        }
        Notifier.error(messageOf(failed));
    }

    private static String codeOf(FailedResponseBean failed) {
        String body = failed.getBody();
        if (body == null || body.isEmpty()) {
            return null;
        }
        try {
            JsPropertyMap<Object> json = Js.cast(Global.JSON.parse(body));
            Object code = json.get("code");
            return code == null ? null : Js.asString(code);
        } catch (Exception e) {
            return null;
        }
    }

    /** Дістає message і details з JSON {code, message, details}. */
    static String messageOf(FailedResponseBean failed) {
        String body = failed.getBody();
        if (body != null && !body.isEmpty()) {
            try {
                JsPropertyMap<Object> json = Js.cast(Global.JSON.parse(body));
                String message = Js.asString(json.get("message"));
                Object details = json.get("details");
                if (details != null) {
                    JsArray<Object> arr = Js.cast(details);
                    StringBuilder sb = new StringBuilder(message);
                    for (int i = 0; i < arr.length; i++) {
                        sb.append("\n• ").append(Js.asString(arr.getAt(i)));
                    }
                    return sb.toString();
                }
                return message;
            } catch (Exception ignored) {
                // відповідь не JSON — нижче покажемо загальне повідомлення
            }
        }
        if (failed.getStatusCode() == 0) {
            return "Сервер недоступний";
        }
        return "Помилка " + failed.getStatusCode();
    }
}
