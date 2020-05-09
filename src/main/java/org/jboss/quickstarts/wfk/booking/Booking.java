package org.jboss.quickstarts.wfk.booking;

import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.taxi.Taxi;
import org.jboss.quickstarts.wfk.travelagent.TravelAgent;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@NamedQueries({
        @NamedQuery(name = Booking.FIND_ALL,
                query = "SELECT b FROM Booking b"),
        @NamedQuery(name = Booking.FIND_BY_ID,
                query = "SELECT b FROM Booking b WHERE b.id = :id"),
        @NamedQuery(name = Booking.FIND_ALL_BY_CUSTOMER_ID,
                query = "SELECT b FROM Booking b WHERE b.customer = :customer_id"),
        @NamedQuery(name = Booking.FIND_ALL_BY_DATE,
                query = "SELECT b FROM Booking b WHERE b.bookingDate = :booking_date")
})
@XmlRootElement
@Table(name = "booking", uniqueConstraints = @UniqueConstraint(columnNames = {"booking_date","taxi_id"}))
public class Booking  implements Serializable {

    public static final String FIND_ALL = "Booking.findAll";
    public static final String FIND_BY_ID = "Booking.findById";
    public static final String FIND_ALL_BY_CUSTOMER_ID = "Booking.findAllByCustomerId";
    public static final String FIND_ALL_BY_DATE = "Booking.findAllByDate";

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "booking_id")
//    @JoinColumns({
//            @JoinColumn(name="customer_id", referencedColumnName="customer_id"),
//            @JoinColumn(name="taxi_id", referencedColumnName="taxi_id")
//    })
    private Long id;

    @NotNull
    @Future(message = "Booking dates can not be in the past. Please choose one from the future")
    @Column(name = "booking_date")
    @Temporal(TemporalType.DATE)
    private Date bookingDate;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    Customer customer;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="taxi_id")
    Taxi taxi;

    @Nullable
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="taxi_booking_id")
    TravelAgent travelAgent;

    public Booking(){}

    public Booking(Customer customer, Taxi taxi, Date bookingDate) {
        this.customer = customer;
        this.taxi = taxi;
        this.bookingDate = bookingDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Taxi getTaxi() {
        return taxi;
    }

    public void setTaxi(Taxi taxi) {
        this.taxi = taxi;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", bookingDate=" + bookingDate +
                ", customer=" + customer +
                ", taxi=" + taxi +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
