package dev.weredok.library.frontend.pages;

import dev.weredok.library.frontend.rest.AuthServiceFactory;
import dev.weredok.library.frontend.ui.Api;
import dev.weredok.library.frontend.ui.Dom;
import dev.weredok.library.frontend.ui.Ui;
import dev.weredok.library.shared.dto.CurrentUserDto;
import dev.weredok.library.shared.dto.LoginRequest;
import elemental2.dom.HTMLElement;
import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.forms.TextBox;

import java.util.function.Consumer;

public class LoginPage {

    private final HTMLElement root = Dom.div("login");
    private final TextBox username = Ui.textBox("Логін");
    private final TextBox password = Ui.password("Пароль");
    private final Button submit;
    private final Consumer<CurrentUserDto> onLogin;

    public LoginPage(Consumer<CurrentUserDto> onLogin) {
        this.onLogin = onLogin;
        submit = Ui.primary("Увійти", this::login);
        Ui.onEnter(password, this::login);

        HTMLElement card = Dom.div("login-card");
        Dom.append(card,
                Dom.text("h2", null, "📚 Бібліотека"),
                Dom.text("p", "muted", "Вхід для працівників"),
                username.element(),
                password.element(),
                submit.element());
        root.appendChild(card);
    }

    private void login() {
        LoginRequest req = new LoginRequest();
        req.setUsername(Ui.value(username).trim());
        req.setPassword(Ui.value(password));
        if (req.getUsername().isEmpty() || req.getPassword().isEmpty()) {
            return;
        }
        Ui.setEnabled(submit, false);
        Api.send(AuthServiceFactory.INSTANCE.login(req), onLogin, () -> Ui.setEnabled(submit, true));
    }

    public HTMLElement element() {
        return root;
    }
}
