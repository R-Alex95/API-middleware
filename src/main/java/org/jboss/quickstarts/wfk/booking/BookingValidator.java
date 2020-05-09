package org.jboss.quickstarts.wfk.booking;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookingValidator {
    @Inject
    private Validator validator;

    @Inject
    private BookingRepository crud;

    void validateBooking(Booking booking) throws ConstraintViolationException, ValidationException {
        Set<ConstraintViolation<Booking>> violations = validator.validate(booking);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        if (dateTaxiCombinationExists(booking)) {
            throw new UniqueBookingException("Unique Booking Violation");
        }
    }

    /**
     * Check if Date and Taxi booking combination is unique.
     *
     * Search if there are bookings on the same date. If yes, search if any of them
     * have the same taxi already booked.
     *
     * @param booking that is about to be checked
     * @return true if unique combination already exists else return no.
     */
    private boolean dateTaxiCombinationExists(Booking booking) {

        List<Booking> bookingsByDate = crud.findAllByDate(booking.getBookingDate());

        for (Booking bookingOnSameDate:bookingsByDate) {
          if (bookingOnSameDate.getTaxi() == booking.getTaxi()) {
              return true;
          }
        }

        return false;
    }
}
