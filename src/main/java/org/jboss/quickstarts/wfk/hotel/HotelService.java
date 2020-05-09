package org.jboss.quickstarts.wfk.hotel;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/api/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface HotelService {
    @POST
    HotelBooking createHotelBooking(HotelBooking booking);

    @DELETE
    @Path("/{id:[0-9]+}")
    Response deleteBooking(@PathParam("id") long id);

}
