package dev.weredok.library.frontend.ui;

/** «Ви впевнені?» перед незворотними діями. */
public final class Confirm {

    private Confirm() {
    }

    public static void ask(String title, String text, String confirmText, Runnable onConfirm) {
        Modal m = new Modal(title);
        m.add(Dom.text("p", null, text));
        m.button(Ui.button("Скасувати", m::close));
        m.button(Ui.danger(confirmText, () -> {
            m.close();
            onConfirm.run();
        }));
        m.open();
    }
}
