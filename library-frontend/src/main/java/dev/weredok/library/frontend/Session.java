package dev.weredok.library.frontend;

import dev.weredok.library.shared.dto.CurrentUserDto;

/** Хто зараз увійшов. Права все одно перевіряє сервер — тут лише щоб ховати зайві кнопки. */
public final class Session {

    private static CurrentUserDto user;

    private Session() {
    }

    public static void set(CurrentUserDto u) {
        user = u;
    }

    public static CurrentUserDto user() {
        return user;
    }

    public static boolean isAdmin() {
        return user != null && "ADMIN".equals(user.getRole());
    }
}
