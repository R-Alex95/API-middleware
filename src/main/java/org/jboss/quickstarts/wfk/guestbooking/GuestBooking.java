package org.jboss.quickstarts.wfk.guestbooking;

import org.jboss.quickstarts.wfk.customer.CustomerInfo;

import java.io.Serializable;
import java.util.Date;

public class GuestBooking implements Serializable {
    CustomerInfo customerInfo;
    Long taxi_id;
    Date booking_date;

    public GuestBooking(){}

    public GuestBooking(CustomerInfo customerInfo, Long taxi_id, Date booking_date) {
        this.customerInfo = customerInfo;
        this.taxi_id = taxi_id;
        this.booking_date = booking_date;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

    public Long getTaxi_id() {
        return taxi_id;
    }

    public void setTaxi_id(Long taxi_id) {
        this.taxi_id = taxi_id;
    }

    public Date getBooking_date() {
        return booking_date;
    }

    public void setBooking_date(Date booking_date) {
        this.booking_date = booking_date;
    }


    @Override
    public String toString() {
        return "GuestBookingInfo{" +
                "customerInfo=" + customerInfo +
                ", taxi_id=" + taxi_id +
                ", booking_date=" + booking_date +
                '}';
    }
}
