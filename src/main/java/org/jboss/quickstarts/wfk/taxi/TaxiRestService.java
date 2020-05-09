package org.jboss.quickstarts.wfk.taxi;


import io.swagger.annotations.*;
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

@Path("/taxis")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/taxis", description = "Operations about taxis")
@Stateless
public class TaxiRestService {
    @Inject
    private @Named("logger") Logger log;

    @Inject
    private TaxiService service;

    /**
     * <p>Return all the Taxis.  They are sorted alphabetically by name.</p>
     *
     * <p>The url may optionally include query parameters specifying a Taxi's name</p>
     *
     * <p>Examples: <pre>GET api/taxis?firstname=John</pre>, <pre>GET api/taxis?firstname=John&lastname=Smith</pre></p>
     *
     * @return A Response containing a list of Taxis
     */
    @GET
    @ApiOperation(value = "Fetch all Taxis", notes = "Returns a JSON array of all stored Taxi objects.")
    public Response retrieveAllTaxis() {
        //Create an empty collection to contain the intersection of Taxis to be returned
        List<Taxi> taxis;

        taxis = service.findAllTaxis();

        return Response.ok(taxis).build();
    }

    /**
     * <p>Search for and return a Taxi identified by email address.<p/>
     *
     * <p>Path annotation includes very simple regex to differentiate between email addresses and Ids.
     * <strong>DO NOT</strong> attempt to use this regex to validate email addresses.</p>
     *
     *
     * @param regNo The string parameter value provided as a Taxi's email
     * @return A Response containing a single Taxi
     */
    @GET
    @Cache
    @Path("/regNo/{regNo:([a-zA-Z0-9]){7}}")
    @ApiOperation(
            value = "Fetch a Taxi by Registration Number",
            notes = "Returns a JSON representation of the Taxi object with the provided registration Number."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message ="Taxi found"),
            @ApiResponse(code = 404, message = "Taxi with registration number was not found")
    })
    public Response retrieveTaxisByRegNo(
            @ApiParam(value = "Registration Number of Taxi to be fetched", required = true)
            @PathParam("regNo")
                    String regNo) {

        Taxi taxi;
        try {
            taxi = service.findByRegNo(regNo);
        } catch (NoResultException e) {
            // Verify that the taxi exists. Return 404, if not present.
            throw new RestServiceException("No Taxi with the email " + regNo + " was found!", Response.Status.NOT_FOUND);
        }
        return Response.ok(taxi).build();
    }

    /**
     * <p>Search for and return a Taxi identified by id.</p>
     *
     * @param id The long parameter value provided as a Taxi's id
     * @return A Response containing a single Taxi
     */
    @GET
    @Cache
    @Path("/{id:[0-9]+}")
    @ApiOperation(
            value = "Fetch a Taxi by id",
            notes = "Returns a JSON representation of the Taxi object with the provided id."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message ="Taxi found"),
            @ApiResponse(code = 404, message = "Taxi with id not found")
    })
    public Response retrieveTaxiById(
            @ApiParam(value = "Id of Taxi to be fetched", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
                    long id) {

        Taxi taxi = service.findById(id);
        if (taxi == null) {
            // Verify that the taxi exists. Return 404, if not present.
            throw new RestServiceException("No Taxi with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }
        log.info("findById " + id + ": found Taxi = " + taxi.toString());

        return Response.ok(taxi).build();
    }

    /**
     * <p>Creates a new taxi from the values provided. Performs validation and will return a JAX-RS response with
     * either 201 (Resource created) or with a map of fields, and related errors.</p>
     *
     * @param taxinfo A taxi POJO, passed as JSON input, in order to be copied to a taxi Object
     * which later on will be <i>created</i> via {@link TaxiService#create(Taxi)}
     * @return A Response indicating the outcome of the create operation
     */
    @SuppressWarnings("unused")
    @POST
    @ApiOperation(value = "Add a new Taxi to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Taxi created successfully."),
            @ApiResponse(code = 400, message = "Invalid Taxi supplied in request body"),
            @ApiResponse(code = 409, message = "Taxi supplied in request body conflicts with an existing Taxi"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response createTaxi(
            @ApiParam(value = "JSON representation of Taxi object to be added to the database", required = true)
                    TaxiInfo taxinfo) {

        Taxi taxi = taxinfo.returnTaxiInstance();
        log.info(taxi.toString());
        if (taxi == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

        Response.ResponseBuilder builder;

        try {
            // Go add the new Taxi.
            service.create(taxi);

            // Create a "Resource Created" 201 Response and pass the taxi back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(taxi);


        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniqueTaxiRegNoException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("RegistrationNumber", "That Registration Number is already used, please use a unique one.");
            throw new RestServiceException("Conflict", responseObj, Response.Status.CONFLICT, e);
        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }

        log.info("createTaxi completed. Taxi = " + taxi.toString());
        return builder.build();
    }

    /**
     * <p>Deletes a taxi using the ID provided. If the ID is not present then nothing can be deleted.</p>
     *
     * <p>Will return a JAX-RS response with either 204 NO CONTENT or with a map of fields, and related errors.</p>
     *
     * @param id The Long parameter value provided as the id of the Taxi to be deleted
     * @return A Response indicating the outcome of the delete operation
     */
    @DELETE
    @Path("/{id:[0-9]+}")
    @ApiOperation(value = "Delete a Taxi from the database")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "The taxi has been successfully deleted"),
            @ApiResponse(code = 400, message = "Invalid Taxi id supplied"),
            @ApiResponse(code = 404, message = "Taxi with id not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response deleteTaxi(
            @ApiParam(value = "Id of Taxi to be deleted", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
                    long id) {

        Response.ResponseBuilder builder;

        Taxi taxi = service.findById(id);
        if (taxi == null) {
            // Verify that the taxi exists. Return 404, if not present.
            throw new RestServiceException("No Taxi with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        try {
            service.delete(taxi);

            builder = Response.noContent();

        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }
        log.info("deleteTaxi completed. Taxi = " + taxi.toString());
        return builder.build();
    }

}
