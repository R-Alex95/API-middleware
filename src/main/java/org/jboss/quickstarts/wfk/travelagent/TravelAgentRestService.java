package org.jboss.quickstarts.wfk.travelagent;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.jboss.quickstarts.wfk.booking.Booking;
import org.jboss.quickstarts.wfk.booking.BookingService;
import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.customer.CustomerService;
import org.jboss.quickstarts.wfk.flight.FlightBooking;
import org.jboss.quickstarts.wfk.flight.FlightService;
import org.jboss.quickstarts.wfk.hotel.HotelBooking;
import org.jboss.quickstarts.wfk.hotel.HotelService;
import org.jboss.quickstarts.wfk.taxi.Taxi;
import org.jboss.quickstarts.wfk.taxi.TaxiService;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.resteasy.annotations.cache.Cache;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/travelAgents")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/travelAgents", description = "Operations about travelAgents")
@Stateless
@TransactionManagement(value = TransactionManagementType.BEAN)
public class TravelAgentRestService {

    @Resource
    UserTransaction ut;

    @Inject
    private @Named("logger")
    Logger log;

    @Inject
    private TravelAgentService service;

    @Inject
    private BookingService bookingService;

    @Inject
    private CustomerService customerService;

    @Inject
    private TaxiService taxiService;

    private ResteasyClient client;


    private FlightBooking flightBooking;

    private HotelBooking hotelBooking;

    private FlightService flightService;
    private HotelService hotelService;

    public TravelAgentRestService() {
//        ResteasyWebTarget target = client.target("http://api-deployment-csc8104-190468327.b9ad.pro-us-east-1.openshiftapps.com/api/bookings");
//        ResteasyWebTarget flightTarget = client.target("http://apideployment-csc8104-190450621.b9ad.pro-us-east-1.openshiftapps.com//api/bookings");
        client = new ResteasyClientBuilder().build();

        ResteasyWebTarget targetFlights = client.target("http://apideployment-csc8104-190450621.b9ad.pro-us-east-1.openshiftapps.com/");
        this.flightService = targetFlights.proxy(FlightService.class);

        ResteasyWebTarget targetHotel = client.target("http://api-deployment-csc8104-190468327.b9ad.pro-us-east-1.openshiftapps.com");
        this.hotelService = targetHotel.proxy(HotelService.class);
    }

    /**
     * <p>Return all the TravelAgents.  They are sorted alphabetically by name.</p>
     *
     * <p>The url may optionally include query parameters specifying a TravelAgent's name</p>
     *
     * <p>Examples: <pre>GET api/travelAgents?firstname=John</pre>, <pre>GET api/travelAgents?firstname=John&lastname=Smith</pre></p>
     *
     * @return A Response containing a list of TravelAgents
     */
    @GET
    @ApiOperation(value = "Fetch all TravelAgents", notes = "Returns a JSON array of all stored TravelAgent objects.")
    public Response retrieveAllTravelAgents() {
        //Create an empty collection to contain the intersection of TravelAgents to be returned
        List<TravelAgent> travelAgents;

        travelAgents = service.findAllTravelAgents();

        return Response.ok(travelAgents).build();
    }

    /**
     * <p>Search for and return a TravelAgent identified by id.</p>
     *
     * @param id The long parameter value provided as a TravelAgent's id
     * @return A Response containing a single TravelAgent
     */
    @GET
    @Cache
    @Path("/{id:[0-9]+}")
    @ApiOperation(
            value = "Fetch a TravelAgent by id",
            notes = "Returns a JSON representation of the TravelAgent object with the provided id."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "TravelAgent found"),
            @ApiResponse(code = 404, message = "TravelAgent with id not found")
    })
    public Response retrieveTravelAgentById(
            @ApiParam(value = "Id of TravelAgent to be fetched", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
                    long id) {

        TravelAgent travelAgent = service.findById(id);
        if (travelAgent == null) {
            // Verify that the travelAgent exists. Return 404, if not present.
            throw new RestServiceException("No TravelAgent with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }
        log.info("findById " + id + ": found TravelAgent = " + travelAgent.toString());

        return Response.ok(travelAgent).build();
    }

    /**
     * <p>Creates a new travelAgent from the values provided. Performs validation and will return a JAX-RS response with
     * either 201 (Resource created) or with a map of fields, and related errors.</p>
     *
     * @param travelAgentInfo A travelAgent POJO, passed as JSON input, in order to be copied to a travelAgent Object
     *                        which later on will be <i>created</i> via {@link TravelAgentService#create(TravelAgent)}
     * @return A Response indicating the outcome of the create operation
     */
    @SuppressWarnings("unused")
    @POST
    @ApiOperation(value = "Create a set of bookings for a fixed Customer(= #10001)")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "TravelAgent created successfully."),
            @ApiResponse(code = 400, message = "Invalid TravelAgent supplied in request body"),
            @ApiResponse(code = 409, message = "TravelAgent supplied in request body conflicts with an existing TravelAgent"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response createTravelAgent(
            @ApiParam(value = "JSON representation of TravelAgent object to be added to the database", required = true)
                    TravelAgentInfo travelAgentInfo) {

        if (travelAgentInfo == null) {
            throw new RestServiceException("Empty Input @ POST ", Response.Status.BAD_REQUEST);
        }

        TravelAgent travelAgent = new TravelAgent();//travelAgentInfo.getTravelAgent();
        travelAgent.setFlightId(travelAgentInfo.getFlightId());
        travelAgent.setHotelId(travelAgentInfo.getHotelId());
        travelAgent.setBookingDate(travelAgentInfo.getBookingDate());
        Taxi taxi = taxiService.findById(travelAgentInfo.getTaxiId());
        if (taxi == null) {
            throw new RestServiceException("No Taxi with the id " + travelAgentInfo.getTaxiId() + " was found!", Response.Status.NOT_FOUND);
        }

        Customer customer = customerService.findById(TravelAgent.getTaxiCustomerId());
        if (customer == null) {
            throw new RestServiceException("No Customer with the id " + TravelAgent.getTaxiCustomerId() + " was found!", Response.Status.NOT_FOUND);
        }
        Response.ResponseBuilder builder;
        try {
            ut.begin();
            Booking booking = new Booking(customer, taxi, travelAgentInfo.getBookingDate());
            // create booking for taxi on our database
            booking = bookingService.create(booking);
            // Go add the new TravelAgent.
            travelAgent.setTaxiBooking(booking);

            log.info("TravelAgentService.create() - Creating " + travelAgent.toString());
            FlightBooking flightBooking = new FlightBooking(travelAgent);
            HotelBooking hotelBooking = new HotelBooking(travelAgent);
            ObjectMapper om = new ObjectMapper();

            log.info(String.format("Hotelbooking - %s", om.writeValueAsString(hotelBooking)));
            log.info(String.format("Flightbooking - %s", om.writeValueAsString(flightBooking)));


//            Response flightResponse = flightTarget.request().post(Entity.entity(hotelBooking, MediaType.APPLICATION_JSON));
            FlightBooking flightBookingResponseObject = flightService.createFlightBooking(flightBooking);
            log.info("FLIGHT: POST request @ " + flightBooking.toString());
            travelAgent.setFlightBookingId(flightBookingResponseObject.getBookingId());
//            log.info("flightResponse " + flightResponse.getStatusInfo() + " " + flightResponse.toString());
//            log.info(flightResponse.readEntity(FlightBooking.class).toString());
//            flightResponse.close();

//            log.info("resp " + flightResp.getStatusInfo() + " " + flightResp.toString());
            log.info("HOTEL: POST request @ " + hotelBooking.toString());
            HotelBooking hotelBookingResponseObject = hotelService.createHotelBooking(hotelBooking);
//            log.info("Hotel booking was created with id#"+hotelBooking.getId());
//            Response response = target.request().post(Entity.entity(hotelBooking, MediaType.APPLICATION_JSON));
//            log.info("resp " + response.getStatusInfo() + " " + response.toString());
//            log.info(response.readEntity(HotelBooking.class).toString());
//            response.close();

            travelAgent.setHotelBookingId(hotelBookingResponseObject.getId());

            service.create(travelAgent);
            // Create a "Resource Created" 201 Response and pass the travelAgent back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(travelAgent);

            ut.commit();

        } catch (ClientErrorException e) {

            this.rollback(travelAgent);
            if (e.getResponse().getStatusInfo() == Response.Status.NOT_FOUND) {
                throw new RestServiceException("API request not found ", e);
            } else {
                e.printStackTrace();
                log.severe(e.getResponse().readEntity(String.class));
                throw new RestServiceException("API request failed due to: " + e.getMessage(), e);
            }
        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            this.rollback(travelAgent);
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (Exception e) {
            // Handle generic exceptions
            this.rollback(travelAgent);
            throw new RestServiceException(e.getMessage(), e);
        }

        log.info("createTravelAgent completed. TravelAgent = " + travelAgent.toString());
        return builder.build();
    }

    /**
     * <p>Deletes a travelAgent using the ID provided. If the ID is not present then nothing can be deleted.</p>
     *
     * <p>Will return a JAX-RS response with either 204 NO CONTENT or with a map of fields, and related errors.</p>
     *
     * @param id The Long parameter value provided as the id of the TravelAgent to be deleted
     * @return A Response indicating the outcome of the delete operation
     */
    @DELETE
    @Path("/{id:[0-9]+}")
    @ApiOperation(value = "Delete a TravelAgent from the database")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "The travelAgent has been successfully deleted"),
            @ApiResponse(code = 400, message = "Invalid TravelAgent id supplied"),
            @ApiResponse(code = 404, message = "TravelAgent with id not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response deleteTravelAgent(
            @ApiParam(value = "Id of TravelAgent to be deleted", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
                    long id) {

        Response.ResponseBuilder builder;

        TravelAgent travelAgent = service.findById(id);
        if (travelAgent == null) {
            // Verify that the travelAgent exists. Return 404, if not present.
            throw new RestServiceException("No TravelAgent with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        try {
            service.delete(travelAgent);

            builder = Response.noContent();

        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }
        log.info("deleteTravelAgent completed. TravelAgent = " + travelAgent.toString());
        return builder.build();
    }

    private void rollback(TravelAgent ta) {
        try {
            // Deletes the objects that were created on the other's end

            if (ta.getFlightBookingId() != null) {
                Response flightDeleteResponse = this.flightService.deleteBooking(ta.getFlightBookingId());
                log.info("Deleting Flight#" + ta.getFlightBookingId() + " Response Status :" + flightDeleteResponse.getStatus());
                flightDeleteResponse.close();
            }
            if (ta.getHotelBookingId() != null) {
                Response hotelDeleteResponse = this.flightService.deleteBooking(ta.getHotelBookingId());
                log.info("Deleting Hotel#" + ta.getHotelBookingId() + " Response Status :" + hotelDeleteResponse.getStatus());
                hotelDeleteResponse.close();
            }
            ut.rollback();
        } catch (SystemException se) {
            se.printStackTrace();
        }
    }

}
