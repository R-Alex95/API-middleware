package org.jboss.quickstarts.wfk.booking;

import io.swagger.annotations.*;
import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.customer.CustomerService;
import org.jboss.quickstarts.wfk.taxi.Taxi;
import org.jboss.quickstarts.wfk.taxi.TaxiService;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.resteasy.annotations.cache.Cache;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/bookings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/bookings", description = "Operations about bookings")
@Stateless
public class BookingRestService {
    @Inject
    private @Named("logger")
    Logger log;

    @Inject
    private CustomerService customerService;

    @Inject
    private TaxiService taxiService;

    @Inject
    private BookingService bookingService;


    /**
     * <p>Return all the Bookings.</p>
     *
     * @return A Response containing a list of Bookings
     */
    @GET
    @ApiOperation(value = "Fetch all Bookings", notes = "Returns a JSON array of all stored Bookings objects.")
    public Response retrieveAllBookings() {
        List<Booking> bookings;

        bookings = bookingService.findAllBookings();

        return Response.ok(bookings).build();
    }

    /**
     * <p>Return all the Bookings made by a particular BookingID.</p>
     *
     * @param id The long parameter value provided as a Customer's=    * @return A Response containing a list of Bookings
     */
    @GET
    @Cache
    @Path("/{id:[0-9]+}")
    @ApiOperation(
            value = "Fetch all Bookings made by a specific CustomerId",
            notes = "Returns a JSON representation of a list of Booking object made by a Customer with the provided id."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message ="Booking(s) found"),
            @ApiResponse(code = 204, message ="No Bookings made by this customer"),
            @ApiResponse(code = 404, message = "Customer with id not found")
    })
    public Response retrieveBookingByCustomerId(
            @ApiParam(value = "Id of Customer to fetch all booking made by that Customer",
                    allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
                    long id) {

        Customer customer = customerService.findById(id);
        if (customer == null) {
            // Verify that the customer exists. Return 404, if not present.
            throw new RestServiceException("No Customer with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }
        List<Booking> bookings = bookingService.findAllByCustomerId(id);

        if (bookings.isEmpty()) {
            throw new RestServiceException("Customer with the id " + id + " has not made any bookings",
                    Response.Status.NO_CONTENT);
        }

        return Response.ok(bookings).build();
    }


    /**
     * <p>Creates a new booking from the values provided. Performs validation and will return a JAX-RS response with
     * either 201 (Resource created) or with a map of fields, and related errors.</p>
     *
     * @param binfo A booking POJO, passed as JSON input, in order to be copied to a taxi Object
     * which later on will be <i>created</i> via {@link BookingService#create(Booking)}
     * @return A Response indicating the outcome of the create operation
     */
    @SuppressWarnings("unused")
    @POST
    @ApiOperation(value = "Add a new Booking to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Booking created successfully."),
            @ApiResponse(code = 400, message = "Invalid Booking supplied in request body"),
            @ApiResponse(code = 404, message = "Customer OR Taxi ID's provided are not found in database"),
            @ApiResponse(code = 409, message = "Booking supplied in request body conflicts with an existing Booking"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response createBooking(
            @ApiParam(value = "JSON representation of Booking object to be added to the database", required = true)
                    BookingInfo binfo)  {

        if (binfo == null) {
            throw new RestServiceException("Invalid input!", Response.Status.BAD_REQUEST);
        }

        if (binfo.getBookingDate().compareTo(new Date()) <=  0 ) {
            throw new RestServiceException("Invalid Date. Date must not be in past!", Response.Status.BAD_REQUEST);
        }

        //log.info(cinfo.toString());
        long custId = binfo.getCustomerID(),
                taxiId = binfo.getTaxiID();

        // Check if customer's & taxi's ids exist.
        Customer customer = customerService.findById(custId);
        if (customer == null) {
            throw new RestServiceException("No Customer with the id " + custId + " was found!", Response.Status.NOT_FOUND);
        }

        Taxi taxi = taxiService.findById(taxiId);
        if (taxi == null) {
            throw new RestServiceException("No Taxi with the id " + taxiId + " was found!", Response.Status.NOT_FOUND);
        }

        Booking booking = new Booking(customer, taxi, binfo.getBookingDate());
        //Booking booking = binfo.returnBookingInstance();
        log.info(booking.toString());

        if (booking == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

        Response.ResponseBuilder builder;

        try {
            // Go add the new Booking.
            bookingService.create(booking);

            // Create a "Resource Created" 201 Response and pass the booking back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(booking);


        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniqueBookingException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("Taxi_Date", "That booking cannot be made. Combination of Taxi & booking date must be unique");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }

        log.info("createBooking completed. Booking = " + booking.toString());
        return builder.build();
    }



    /**
     * <p>Deletes a booking using the ID provided. If the ID is not present then nothing can be deleted.</p>
     *
     * @param id The Long parameter value provided as the id of the Booking to be deleted
     * @return A Response indicating the outcome of the delete operation
     */
    @DELETE
    @Path("/{id:[0-9]+}")
    @ApiOperation(value = "Delete a Booking from the database")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "The booking has been successfully deleted"),
            @ApiResponse(code = 400, message = "Invalid Booking id supplied"),
            @ApiResponse(code = 404, message = "Booking with id not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response deleteBooking(
            @ApiParam(value = "Id of Booking to be deleted", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
                    long id) {

        Response.ResponseBuilder builder;

        Booking booking = bookingService.findById(id);
        if (booking == null) {
            // Verify that the booking exists. Return 404, if not present.
            throw new RestServiceException("No Booking with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        try {
            bookingService.delete(booking);

            builder = Response.noContent();

        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }
        log.info("deleteBooking completed. Booking = " + booking.toString());
        return builder.build();
    }
}
