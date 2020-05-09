package org.jboss.quickstarts.wfk.flight;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/api/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface FlightService {

    @POST
    FlightBooking createFlightBooking(FlightBooking booking);

    @DELETE
    @Path("/{id:[0-9]+}")
    Response deleteBooking(@PathParam("id") long id);

}
