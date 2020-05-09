package org.jboss.quickstarts.wfk.taxi;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.List;
import java.util.logging.Logger;

public class TaxiService {
    @Inject
    private @Named("logger") Logger log;

    @Inject
    private TaxiValidator validator;

    @Inject
    private TaxiRepository crud;

    private ResteasyClient client;

    /**
     * <p>Create a new client which will be used for our outgoing REST client communication</p>
     */
    public TaxiService() {
        // Create client service instance to make REST requests to upstream service
        client = new ResteasyClientBuilder().build();
    }

    /**
     * <p>Returns a single Taxi object, specified by a Long id.<p/>
     *
     * @param id The id field of the Taxi to be returned
     * @return The Taxi with the specified id
     */
    public Taxi findById(Long id) {
        return crud.findById(id);
    }


    /**
     * Returns a List of all persisted {@link Taxi} objects.
     *
     * @return List of Taxi objects
     */
    List<Taxi> findAllTaxis() {
        return crud.findAllTaxis();
    }

    /**
     * <p>Returns a single Taxi object, specified by a String RegNo (= Registration number).<p/>
     *
     * @param regNo The regNo field of the Taxis to be returned
     * @return The Taxis with the specified regNo
     */
    Taxi findByRegNo(String regNo) {
        return crud.findByRegNo(regNo);
    }

    /**
     * <p>Writes the provided Taxi object to the application database.<p/>
     *
     * <p>Validates the data in the provided Taxi object using a {@link TaxiValidator} object.<p/>
     *
     * @param taxi The Taxi object to be written to the database using a {@link TaxiRepository} object
     * @return The Taxi object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Taxi create(Taxi taxi) throws ConstraintViolationException, ValidationException, Exception {
        log.info("TaxiService.create() - Creating " + taxi.getRegNo() + " & " + taxi.getSeats() + " seats");

        // Check to make sure the data fits with the parameters in the Taxi model and passes validation.
        validator.validateTaxi(taxi);

        // Write the taxi to the database.
        return crud.create(taxi);
    }

    /**
     * <p>Deletes the provided Taxi object from the application database if found there.<p/>
     *
     * @param taxi The Taxi object to be removed from the application database
     * @return The Taxi object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    Taxi delete(Taxi taxi) throws Exception {
        log.info("delete() - Deleting " + taxi.toString());

        Taxi deletedTaxi = null;

        if (taxi.getId() != null) {
            deletedTaxi = crud.delete(taxi);
        } else {
            log.info("delete() - No ID was found so can't Delete.");
        }

        return deletedTaxi;
    }

}
