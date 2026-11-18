package dev.weredok.library.frontend.ui;

import elemental2.dom.HTMLElement;
import elemental2.dom.Node;
import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.dialogs.Dialog;

/**
 * Діалог із заголовком, тілом і кнопками внизу. Обгортка над Domino UI Dialog.
 */
public class Modal {

    private final Dialog dialog;
    private final HTMLElement body;
    private final HTMLElement footer;

    public Modal(String title) {
        dialog = Dialog.create();
        HTMLElement root = Dom.div("modal-content-root");
        root.appendChild(Dom.text("h3", "modal-title", title));
        body = Dom.div("modal-body-content");
        footer = Dom.div("modal-footer-content");
        root.appendChild(body);
        root.appendChild(footer);
        dialog.appendChild(root);
    }

    public Modal add(Node node) {
        body.appendChild(node);
        return this;
    }

    public Modal button(Button b) {
        footer.appendChild(b.element());
        return this;
    }

    public void open() {
        dialog.open();
    }

    public void close() {
        dialog.close();
    }
}
