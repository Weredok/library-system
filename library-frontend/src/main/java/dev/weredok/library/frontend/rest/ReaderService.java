package dev.weredok.library.frontend.rest;

import dev.weredok.library.shared.dto.LoanPage;
import dev.weredok.library.shared.dto.ReaderDto;
import dev.weredok.library.shared.dto.ReaderFormDto;
import dev.weredok.library.shared.dto.ReaderPage;
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

@RequestFactory
@Path("api/readers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ReaderService {

    @GET
    ReaderPage search(@QueryParam("q") String q,
                      @QueryParam("page") int page,
                      @QueryParam("size") int size);

    @GET
    @Path("{id}")
    ReaderDto get(@PathParam("id") int id);

    @GET
    @Path("{id}/loans")
    LoanPage history(@PathParam("id") int id,
                     @QueryParam("page") int page,
                     @QueryParam("size") int size);

    @POST
    ReaderDto register(ReaderFormDto body);

    @PUT
    @Path("{id}")
    ReaderDto update(@PathParam("id") int id, ReaderFormDto body);

    @DELETE
    @Path("{id}")
    Void archive(@PathParam("id") int id);
}
