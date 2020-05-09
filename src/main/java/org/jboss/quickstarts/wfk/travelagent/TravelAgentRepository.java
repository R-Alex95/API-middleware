package org.jboss.quickstarts.wfk.travelagent;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.List;
import java.util.logging.Logger;

public class TravelAgentRepository {

    @Inject
    private @Named("logger")
    Logger log;

    @Inject
    private EntityManager em;

    /**
     * Returns a List of all persisted {@link TravelAgent} objects.
     *
     * @return List of TravelAgent objects
     */
    List<TravelAgent> findAllTravelAgents() {
        TypedQuery<TravelAgent> query = em.createNamedQuery(TravelAgent.FIND_ALL, TravelAgent.class);
        return query.getResultList();
    }

    /**
     * <p>Returns a single TravelAgent object, specified by a Long id.<p/>
     *
     * @param id The id field of the TravelAgent to be returned
     * @return The TravelAgent with the specified id
     */
    TravelAgent findById(Long id) {
        return em.find(TravelAgent.class, id);
    }

    /**
     * <p>Persists the provided TravelAgent object to the application database using the EntityManager.</p>
     *
     * <p>{@link EntityManager#persist(Object) persist(Object)} takes an entity instance, adds it to the
     * context and makes that instance managed (ie future updates to the entity will be tracked)</p>
     *
     * <p>persist(Object) will set the @GeneratedValue @Id for an object.</p>
     *
     * @param travelAgent The TravelAgent object to be persisted
     * @return The TravelAgent object that has been persisted
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    TravelAgent create(TravelAgent travelAgent) throws ConstraintViolationException, ValidationException, Exception {
        log.info("TravelAgentRepository.create() - Creating #" + travelAgent.toString());

        // Write the travelAgent to the database.
        em.persist(travelAgent);

        return travelAgent;
    }


    /**
     * <p>Deletes the provided TravelAgent object from the application database if found there</p>
     *
     * @param travelAgent The TravelAgent object to be removed from the application database
     * @return The TravelAgent object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    TravelAgent delete(TravelAgent travelAgent) throws Exception {
        log.info("TravelAgentRepository.delete() - Deleting " + travelAgent.toString());

        if (travelAgent.getId() != null) {
            /*
             * The Hibernate session (aka EntityManager's persistent context) is closed and invalidated after the commit(),
             * because it is bound to a transaction. The object goes into a detached status. If you open a new persistent
             * context, the object isn't known as in a persistent state in this new context, so you have to merge it.
             *
             * Merge sees that the object has a primary key (id), so it knows it is not new and must hit the database
             * to reattach it.
             *
             * Note, there is NO remove method which would just take a primary key (id) and a entity class as argument.
             * You first need an object in a persistent state to be able to delete it.
             *
             * Therefore we merge first and then we can remove it.
             */
            em.remove(em.merge(travelAgent));

        } else {
            log.info("TravelAgentRepository.delete() - No ID was found so can't Delete.");
        }

        return travelAgent;
    }
}
