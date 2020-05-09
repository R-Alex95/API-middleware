package org.jboss.quickstarts.wfk.hotel;

import org.jboss.quickstarts.wfk.travelagent.TravelAgent;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.sql.Date;

/* POJO representing Hotel objects */
public class HotelBooking implements Serializable {
    private Long id;
    private Long customerId;
    private Long hotelId;
    private Date date;

    public HotelBooking() {
    }

    public HotelBooking(TravelAgent ta) {
        hotelId = ta.getHotelId();
//        Date bookingDate = ta.getBookingDate();
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        this.date = dateFormat.format(bookingDate);
        this.date = ta.getBookingDate();
        customerId = TravelAgent.getHotelCustomerId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "HotelBooking{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", hotelId=" + hotelId +
                ", date=" + date +
                '}';
    }

}
