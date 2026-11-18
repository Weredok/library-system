package dev.weredok.library.frontend.rest;

import dev.weredok.library.shared.dto.BookDto;
import dev.weredok.library.shared.dto.BookFormDto;
import dev.weredok.library.shared.dto.BookPage;
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
@Path("api/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BookService {

    /** categoryId = 0 — усі категорії; page з 0. */
    @GET
    BookPage search(@QueryParam("q") String q,
                    @QueryParam("categoryId") int categoryId,
                    @QueryParam("availableOnly") boolean availableOnly,
                    @QueryParam("page") int page,
                    @QueryParam("size") int size);

    @GET
    @Path("{id}")
    BookDto get(@PathParam("id") int id);

    @POST
    BookDto create(BookFormDto body);

    @PUT
    @Path("{id}")
    BookDto update(@PathParam("id") int id, BookFormDto body);

    @DELETE
    @Path("{id}")
    Void archive(@PathParam("id") int id);
}
