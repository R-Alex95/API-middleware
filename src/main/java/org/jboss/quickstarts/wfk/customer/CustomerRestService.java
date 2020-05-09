package org.jboss.quickstarts.wfk.customer;


import io.swagger.annotations.*;
import org.jboss.quickstarts.wfk.area.InvalidAreaCodeException;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.resteasy.annotations.cache.Cache;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/customers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/customers", description = "Operations about customers")
@Stateless

public class CustomerRestService {
    @Inject
    private @Named("logger")
    Logger log;

    @Inject
    private CustomerService service;

    /**
     * <p>Return all the Customers.  They are sorted alphabetically by name.</p>
     *
     * <p>The url may optionally include query parameters specifying a Customer's name</p>
     *
     * <p>Examples: <pre>GET api/customers?firstname=John</pre>, <pre>GET api/customers?firstname=John&lastname=Smith</pre></p>
     *
     * @return A Response containing a list of Customers
     */
    @GET
    @ApiOperation(value = "Fetch all Customers", notes = "Returns a JSON array of all stored Customer objects.")
    public Response retrieveAllCustomers() {
        List<Customer> customers;

        customers = service.findAllOrderedByName();

        return Response.ok(customers).build();
    }

    /**
     * <p>Search for and return a Customer identified by id.</p>
     *
     * @param id The long parameter value provided as a Customer's id
     * @return A Response containing a single Customer
     */
    @GET
    @Cache
    @Path("/{id:[0-9]+}")
    @ApiOperation(
            value = "Fetch a Customer by id",
            notes = "Returns a JSON representation of the Customer object with the provided id."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message ="Customer found"),
            @ApiResponse(code = 404, message = "Customer with id not found")
    })
    public Response retrieveCustomerById(
            @ApiParam(value = "Id of Customer to be fetched", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
                    long id) {

        Customer customer = service.findById(id);
        if (customer == null) {
            // Verify that the customer exists. Return 404, if not present.
            throw new RestServiceException("No Customer with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }
        log.info("findById " + id + ": found Customer = " + customer.toString());

        return Response.ok(customer).build();
    }
    /**
     * <p>Search for and return a Customer identified by email address.<p/>
     *
     * <p>Path annotation includes very simple regex to differentiate between email addresses and Ids.
     * <strong>DO NOT</strong> attempt to use this regex to validate email addresses.</p>
     *
     *
     * @param email The string parameter value provided as a Customer's email
     * @return A Response containing a single Customer
     */
    @GET
    @Cache
    @Path("/email/{email:.+[%40|@].+}")
    @ApiOperation(
            value = "Fetch a Customer by Email",
            notes = "Returns a JSON representation of the Customer object with the provided email."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message ="Customer found"),
            @ApiResponse(code = 404, message = "Customer with email not found")
    })
    public Response retrieveCustomersByEmail(
            @ApiParam(value = "Email of Customer to be fetched", required = true)
            @PathParam("email")
                    String email) {

        Customer customer;
        try {
            customer = service.findByEmail(email);
        } catch (NoResultException e) {
            // Verify that the customer exists. Return 404, if not present.
            throw new RestServiceException("No Customer with the email " + email + " was found!", Response.Status.NOT_FOUND);
        }
        return Response.ok(customer).build();
    }

    /**
     * <p>Creates a new customer from the values provided. Performs validation and will return a JAX-RS response with
     * either 201 (Resource created) or with a map of fields, and related errors.</p>
     *
     * @param cinfo A customer POJO, passed as JSON input, in order to be copied to a taxi Object
     * which later on will be <i>created</i> via {@link CustomerService#create(Customer)}
     * @return A Response indicating the outcome of the create operation
     */
    @SuppressWarnings("unused")
    @POST
    @ApiOperation(value = "Add a new Customer to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Customer created successfully."),
            @ApiResponse(code = 400, message = "Invalid Customer supplied in request body"),
            @ApiResponse(code = 409, message = "Customer supplied in request body conflicts with an existing Customer"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response createCustomer(
            @ApiParam(value = "JSON representation of Customer object to be added to the database", required = true)
                    CustomerInfo cinfo) {

        //log.info(cinfo.toString());
        Customer customer = cinfo.returnCustomerInstance();
        log.info(customer.toString());

        if (customer == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

        Response.ResponseBuilder builder;

        try {
            // Go add the new Customer.
            service.create(customer);

            // Create a "Resource Created" 201 Response and pass the customer back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(customer);

        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniqueCustomerEmailException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("email", "That email is already used, please use a unique email");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }

        log.info("createCustomer completed. Customer = " + customer.toString());
        return builder.build();
    }

    /**
     * <p>Deletes a customer using the ID provided. If the ID is not present then nothing can be deleted.</p>
     *
     * <p>Will return a JAX-RS response with either 204 NO CONTENT or with a map of fields, and related errors.</p>
     *
     * @param id The Long parameter value provided as the id of the Customer to be deleted
     * @return A Response indicating the outcome of the delete operation
     */
    @DELETE
    @Path("/{id:[0-9]+}")
    @ApiOperation(value = "Delete a Customer from the database")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "The customer has been successfully deleted"),
            @ApiResponse(code = 400, message = "Invalid Customer id supplied"),
            @ApiResponse(code = 404, message = "Customer with id not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response deleteCustomer(
            @ApiParam(value = "Id of Customer to be deleted", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
                    long id) {

        Response.ResponseBuilder builder;

        Customer customer = service.findById(id);
        if (customer == null) {
            // Verify that the customer exists. Return 404, if not present.
            throw new RestServiceException("No Customer with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        try {
            service.delete(customer);

            builder = Response.noContent();

        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }
        log.info("deleteCustomer completed. Customer = " + customer.toString());
        return builder.build();
    }

}
