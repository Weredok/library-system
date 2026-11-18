package dev.weredok.library.frontend.rest;

import dev.weredok.library.shared.dto.OverdueDto;
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

import java.util.List;

@RequestFactory
@Path("api/overdue")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface OverdueService {

    @GET
    List<OverdueDto> list();
}
