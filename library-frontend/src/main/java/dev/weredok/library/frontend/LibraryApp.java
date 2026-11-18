package dev.weredok.library.frontend;

import com.google.gwt.core.client.EntryPoint;
import dev.weredok.library.frontend.layout.AppShell;
import dev.weredok.library.frontend.pages.LoginPage;
import dev.weredok.library.frontend.rest.AuthServiceFactory;
import dev.weredok.library.frontend.ui.Api;
import dev.weredok.library.frontend.ui.Dom;
import dev.weredok.library.shared.dto.CurrentUserDto;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import jsinterop.base.Js;
import org.dominokit.rest.DominoRestConfig;

/** Точка входу GWT. */
public class LibraryApp implements EntryPoint {

    private HTMLElement mount;

    @Override
    public void onModuleLoad() {
        mount = Js.uncheckedCast(DomGlobal.document.getElementById("app"));

        DominoRestConfig.initDefaults();
        // Бекенд і фронт на одному origin — запити йдуть на /api/...
        DominoRestConfig.getInstance().setDefaultServiceRoot(DomGlobal.location.origin + "/");

        Api.setOnUnauthorized(this::showLogin);

        // Якщо сесія жива (оновили сторінку) — одразу в систему, інакше 401 → екран входу
        Api.send(AuthServiceFactory.INSTANCE.me(), this::start);
    }

    private void start(CurrentUserDto user) {
        Session.set(user);
        Dom.clear(mount);
        AppShell shell = new AppShell(this::logout);
        mount.appendChild(shell.element());
        new Router(shell).route();
    }

    private void showLogin() {
        Session.set(null);
        Dom.clear(mount);
        mount.appendChild(new LoginPage(this::start).element());
    }

    private void logout() {
        Api.send(AuthServiceFactory.INSTANCE.logout(), ignored -> {
            DomGlobal.location.hash = "";
            showLogin();
        });
    }
}
