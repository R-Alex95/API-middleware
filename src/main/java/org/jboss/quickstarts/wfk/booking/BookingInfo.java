package org.jboss.quickstarts.wfk.booking;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Embeddable
public class BookingInfo implements Serializable {

    @NotNull
    private long customerID;

    @NotNull
    private long taxiID;

    @NotNull
    @Future(message = "Booking dates can not be in the past. Please choose one from the future")
    @Temporal(TemporalType.DATE)
    private Date bookingDate;

    public BookingInfo() {}

    public BookingInfo(long customerID, long taxiID, Date bookingDate) {
        this.customerID = customerID;
        this.taxiID = taxiID;
        this.bookingDate = bookingDate;
    }

    public long getCustomerID() {
        return customerID;
    }

    public void setCustomerID(long customerID) {
        this.customerID = customerID;
    }

    public long getTaxiID() {
        return taxiID;
    }

    public void setTaxiID(long taxiID) {
        this.taxiID = taxiID;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

//    public Booking getBookingInstance() {
//        return new Booking(customerID, taxiID, bookingDate);
//    }
    @Override
    public String toString() {
        return "BookingInfo{" +
                "customerID=" + customerID +
                ", taxiID=" + taxiID +
                ", bookingDate=" + bookingDate +
                '}';
    }
}
