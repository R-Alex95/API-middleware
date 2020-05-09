package org.jboss.quickstarts.wfk.guestbooking;

import io.swagger.annotations.*;
import org.jboss.quickstarts.wfk.booking.Booking;
import org.jboss.quickstarts.wfk.booking.BookingService;
import org.jboss.quickstarts.wfk.booking.UniqueBookingException;
import org.jboss.quickstarts.wfk.contact.UniqueEmailException;
import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.customer.CustomerService;
import org.jboss.quickstarts.wfk.taxi.Taxi;
import org.jboss.quickstarts.wfk.taxi.TaxiService;
import org.jboss.quickstarts.wfk.util.RestServiceException;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Stateless
@Path("/guestbooking")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/guestbooking", description = "Operations about guestbooking")

@TransactionManagement(value = TransactionManagementType.BEAN)
public class GuestBookingRestService {

    @Resource
    UserTransaction ut;

    @Inject
    private CustomerService customerService;

    @Inject
    private BookingService bookingService;

    @Inject
    private TaxiService taxiService;

    @Inject
    @Named("logger")
    Logger log;

    @SuppressWarnings("unused")
    @POST
    @ApiOperation(value = "Add a new customer & a booking to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "GuestBooking created successfully."),
            @ApiResponse(code = 400, message = "Invalid GuestBooking supplied in request body"),
            @ApiResponse(code = 409, message = "GuestBooking supplied in request body conflicts with an existing GuestBooking"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response createGuestBooking(
            @ApiParam(value = "JSON representation of GuestBookingInfo object.", required = true)
                    GuestBooking guestBooking) {

        if (guestBooking == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

        Customer customer = guestBooking.getCustomerInfo().returnCustomerInstance();

        if (customer == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

        Taxi taxi = taxiService.findById(guestBooking.getTaxi_id());
        if (taxi == null) {
            throw new RestServiceException("No Taxi with the id " + guestBooking.getTaxi_id() + " was found!", Response.Status.BAD_REQUEST);
        }

        Response.ResponseBuilder builder;

        try {
            // Go add the new GuestBooking.

            ut.begin();
            // Try to persist customer Object
            customerService.create(customer);
            builder = Response.status(Response.Status.CREATED).entity(customer);

            Booking booking = new Booking(customer, taxi, guestBooking.getBooking_date());

            bookingService.create(booking);

            builder = Response.status(Response.Status.CREATED).entity(booking);
            ut.commit();
            log.info("createGuestBooking completed. GuestBooking = " + guestBooking.toString());
            return builder.build();
        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            try {
                ut.rollback();
            } catch (SystemException e) {
                e.printStackTrace();
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniqueEmailException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("email", "That email is already used, please use a unique email");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (UniqueBookingException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("booking", "Taxi is already booked that specific date. Try another combination.");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (Exception e) {
            // Handle generic exceptions
            try {
                ut.rollback();
            } catch (SystemException ex) {
                ex.printStackTrace();
            }
            throw new RestServiceException(e);
        }
    }

}
