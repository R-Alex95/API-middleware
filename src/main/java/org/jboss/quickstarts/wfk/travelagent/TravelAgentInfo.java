package org.jboss.quickstarts.wfk.travelagent;

import org.checkerframework.checker.index.qual.Positive;

import javax.persistence.Embeddable;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.sql.Date;

@Embeddable
public class TravelAgentInfo implements Serializable {

    @NotNull
    @Positive
    private long flightId;
    @NotNull
    @Positive
    private long taxiId;
    @NotNull
    @Positive
    private long hotelId;
    @NotNull
    @Future
    Date bookingDate;

    public long getFlightId() {
        return flightId;
    }

    public void setFlightId(long flightId) {
        this.flightId = flightId;
    }

    public long getTaxiId() {
        return taxiId;
    }

    public void setTaxiId(long taxiId) {
        this.taxiId = taxiId;
    }

    public long getHotelId() {
        return hotelId;
    }

    public void setHotelId(long hotelId) {
        this.hotelId = hotelId;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }
}
