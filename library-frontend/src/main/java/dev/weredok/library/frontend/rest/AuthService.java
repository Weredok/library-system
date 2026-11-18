package dev.weredok.library.frontend.rest;

import dev.weredok.library.shared.dto.CurrentUserDto;
import dev.weredok.library.shared.dto.LoginRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;

/** Domino REST згенерує з цього інтерфейсу клас AuthServiceFactory. */
@RequestFactory
@Path("api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface AuthService {

    @POST
    @Path("login")
    CurrentUserDto login(LoginRequest body);

    @GET
    @Path("me")
    CurrentUserDto me();

    @POST
    @Path("logout")
    Void logout();
}
