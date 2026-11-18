package dev.weredok.library.frontend.rest;

import dev.weredok.library.shared.dto.IssueLoanRequest;
import dev.weredok.library.shared.dto.LoanDto;
import dev.weredok.library.shared.dto.LoanPage;
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
@Path("api/loans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface LoanService {

    /** filter — назва з LoanFilter; readerId / bookId = 0 — без фільтру. */
    @GET
    LoanPage search(@QueryParam("filter") String filter,
                    @QueryParam("readerId") int readerId,
                    @QueryParam("bookId") int bookId,
                    @QueryParam("page") int page,
                    @QueryParam("size") int size);

    @POST
    LoanDto issue(IssueLoanRequest body);

    @POST
    @Path("{id}/return")
    LoanDto returnBook(@PathParam("id") int id);

    @POST
    @Path("{id}/renew")
    LoanDto renew(@PathParam("id") int id);
}
