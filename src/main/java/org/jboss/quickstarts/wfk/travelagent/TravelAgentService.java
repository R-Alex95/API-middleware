package org.jboss.quickstarts.wfk.travelagent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.quickstarts.wfk.flight.FlightBooking;
import org.jboss.quickstarts.wfk.flight.FlightService;
import org.jboss.quickstarts.wfk.hotel.HotelBooking;
import org.jboss.quickstarts.wfk.hotel.HotelService;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;
public class TravelAgentService {

    @Inject
    private @Named("logger") Logger log;

    @Inject
    private TravelAgentRepository crud;


    /**
     * <p>Returns a single TravelAgent object, specified by a Long id.<p/>
     *
     * @param id The id field of the TravelAgent to be returned
     * @return The TravelAgent with the specified id
     */
    public TravelAgent findById(Long id) {
        return crud.findById(id);
    }


    /**
     * Returns a List of all persisted {@link TravelAgent} objects.
     *
     * @return List of TravelAgent objects
     */
    List<TravelAgent> findAllTravelAgents() {
        return crud.findAllTravelAgents();
    }

    /**
     * <p>Writes the provided TravelAgent object to the application database.<p/>
     *
     * @param travelAgent The TravelAgent object to be written to the database using a {@link TravelAgentRepository} object
     * @return The TravelAgent object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    TravelAgent create(TravelAgent travelAgent) throws ConstraintViolationException, ValidationException, Exception {
        // Write the travelAgent to the database.
        return crud.create(travelAgent);
    }

    /**
     * <p>Deletes the provided TravelAgent object from the application database if found there.<p/>
     *
     * @param travelAgent The TravelAgent object to be removed from the application database
     * @return The TravelAgent object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    TravelAgent delete(TravelAgent travelAgent) throws Exception {
        log.info("delete() - Deleting " + travelAgent.toString());

        TravelAgent deletedTravelAgent = null;

        if (travelAgent.getId() != null) {
            deletedTravelAgent = crud.delete(travelAgent);
        } else {
            log.info("delete() - No ID was found so can't Delete.");
        }

        return deletedTravelAgent;
    }

}
