package org.jboss.quickstarts.wfk.taxi;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.List;
import java.util.logging.Logger;

public class TaxiRepository {

    @Inject
    private @Named("logger")
    Logger log;

    @Inject
    private EntityManager em;

    /**
     * Returns a List of all persisted {@link Taxi} objects.
     *
     * @return List of Taxi objects
     */
    List<Taxi> findAllTaxis() {
        TypedQuery<Taxi> query = em.createNamedQuery(Taxi.FIND_ALL, Taxi.class);
        return query.getResultList();
    }

    /**
     * <p>Returns a single Taxi object, specified by a Long id.<p/>
     *
     * @param id The id field of the Taxi to be returned
     * @return The Taxi with the specified id
     */
    Taxi findById(Long id) {
        return em.find(Taxi.class, id);
    }

    /**
     * Returns a single Taxi object, specified by a String registration number.
     * If there is more than one Taxi with the specified registration number, only the first encountered will be returned.
     *
     * @param regNo The registration Number of the Taxi to be returned
     * @return The first Taxi with the specified regNo
     */
    Taxi findByRegNo(String regNo) {
        TypedQuery<Taxi> query = em.createNamedQuery(Taxi.FIND_BY_REGNO, Taxi.class)
                .setParameter("regNo", regNo);
        return query.getSingleResult();
    }

    /**
     * <p>Persists the provided Taxi object to the application database using the EntityManager.</p>
     *
     * <p>{@link EntityManager#persist(Object) persist(Object)} takes an entity instance, adds it to the
     * context and makes that instance managed (ie future updates to the entity will be tracked)</p>
     *
     * <p>persist(Object) will set the @GeneratedValue @Id for an object.</p>
     *
     * @param taxi The Taxi object to be persisted
     * @return The Taxi object that has been persisted
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Taxi create(Taxi taxi) throws ConstraintViolationException, ValidationException, Exception {
        log.info("TaxiRepository.create() - Creating #" + taxi.getRegNo() + " with " + taxi.getSeats() + " seats");

        // Write the taxi to the database.
        em.persist(taxi);

        return taxi;
    }


    /**
     * <p>Deletes the provided Taxi object from the application database if found there</p>
     *
     * @param taxi The Taxi object to be removed from the application database
     * @return The Taxi object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    Taxi delete(Taxi taxi) throws Exception {
        log.info("TaxiRepository.delete() - Deleting " + taxi.toString());

        if (taxi.getId() != null) {
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
            em.remove(em.merge(taxi));

        } else {
            log.info("TaxiRepository.delete() - No ID was found so can't Delete.");
        }

        return taxi;
    }
}
