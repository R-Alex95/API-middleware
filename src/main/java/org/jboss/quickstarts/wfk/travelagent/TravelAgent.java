package org.jboss.quickstarts.wfk.travelagent;

import org.checkerframework.checker.index.qual.Positive;
import org.jboss.quickstarts.wfk.booking.Booking;
import org.jboss.quickstarts.wfk.customer.Customer;

import javax.persistence.*;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.sql.Date;


@Entity
@NamedQueries({
        @NamedQuery(name = TravelAgent.FIND_ALL, query = "SELECT t FROM TravelAgent t"),
        @NamedQuery(name = TravelAgent.FIND_BY_ID, query = "SELECT t FROM TravelAgent t WHERE t.id = :id")
})
@XmlRootElement
@Table(name = "travelAgent")
public class TravelAgent implements Serializable {

    public static final String FIND_ALL = "TravelAgent.findAll";
    public static final String FIND_BY_ID = "TravelAgent.findById";

    private static final long HOTEL_CUSTOMER_ID = 10003;
    private static final long FLIGHT_CUSTOMER_ID = 701;
    private static final long TAXI_CUSTOMER_ID = 10001;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @NotNull
    @Positive
    private Long flightId;

    @NotNull
    @Positive
    private Long flightBookingId;

    @NotNull
    @Positive
    private Long hotelId;
    @NotNull
    @Positive
    private Long hotelBookingId;

    @OneToOne
    @JoinColumn(name="taxi_booking_id")
    Booking taxiBooking;

    @NotNull
    @Future
    Date bookingDate; // booking Date for all commodities

    public static Long getHotelCustomerId() {
        return HOTEL_CUSTOMER_ID;
    }

    public static Long getFlightCustomerId() {
        return FLIGHT_CUSTOMER_ID;
    }

    public static Long getTaxiCustomerId() {
        return TAXI_CUSTOMER_ID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public Booking getTaxiBooking() {
        return taxiBooking;
    }

    public void setTaxiBooking(Booking taxiBooking) {
        this.taxiBooking = taxiBooking;
    }

    public Long getFlightBookingId() {
        return flightBookingId;
    }

    public void setFlightBookingId(Long flightBookingId) {
        this.flightBookingId = flightBookingId;
    }

    public Long getHotelBookingId() {
        return hotelBookingId;
    }

    public void setHotelBookingId(Long hotelBookingId) {
        this.hotelBookingId = hotelBookingId;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    @Override
    public String toString() {
        return "TravelAgent{" +
                "id=" + id +
                ", flightId=" + flightId +
                ", flightBookingId=" + flightBookingId +
                ", hotelId=" + hotelId +
                ", hotelBookingId=" + hotelBookingId +
                ", taxiBooking=" + taxiBooking +
                ", bookingDate=" + bookingDate +
                '}';
    }
}
