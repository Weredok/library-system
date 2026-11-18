package dev.weredok.library.frontend.ui;

import org.dominokit.domino.ui.notifications.Notification;

/** Спливаючі повідомлення (Domino UI Notification). */
public final class Notifier {

    private Notifier() {
    }

    public static void success(String message) {
        Notification.create(message).show();
    }

    public static void error(String message) {
        Notification n = Notification.create(message);
        n.element().classList.add("notification-error");
        n.show();
    }
}
