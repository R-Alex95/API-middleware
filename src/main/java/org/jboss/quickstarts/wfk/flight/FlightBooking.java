package org.jboss.quickstarts.wfk.flight;

import org.jboss.quickstarts.wfk.travelagent.TravelAgent;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.sql.Date;

/* POJO representing Flight objects */
@Embeddable
public class FlightBooking  {
    private Long bookingId;
    private Long customerId;
    private Long flightId;
    private Date bookingDate;

    public FlightBooking() {
    }

    public FlightBooking(TravelAgent ta) {
        flightId = ta.getFlightId();
        bookingDate = ta.getBookingDate();
        customerId = TravelAgent.getFlightCustomerId();
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    @Override
    public String toString() {
        return "FlightBooking{" +
                "bookingId=" + bookingId +
                ", customerId=" + customerId +
                ", flightId=" + flightId +
                ", bookingDate=" + bookingDate +
                '}';
    }
}
