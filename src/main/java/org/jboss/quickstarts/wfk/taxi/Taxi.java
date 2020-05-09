package org.jboss.quickstarts.wfk.taxi;

import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.quickstarts.wfk.booking.Booking;

import javax.persistence.*;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;


@Entity
@NamedQueries({
        @NamedQuery(name = Taxi.FIND_ALL, query = "SELECT t FROM Taxi t ORDER BY t.seats ASC"),
        @NamedQuery(name = Taxi.FIND_BY_REGNO, query = "SELECT t FROM Taxi t WHERE t.regNo =:regNo")
})
@XmlRootElement
@Table(name = "taxi", uniqueConstraints = @UniqueConstraint(columnNames = {"regNo"}))
public class Taxi implements Serializable {

    public static final String FIND_ALL = "Taxi.findAll";
    public static final String FIND_BY_REGNO = "Taxi.findByRegNo";

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @NotNull
    @Size(min = 1, max = 7)
    @Pattern(regexp = "^([a-zA-Z0-9]){7}$",
            message = "The registration number must be an alphanumeric string of length 7. Spaces not allowed")
    private String regNo;

    @NotNull
    @Min(2)
    @Max(20)
    @Column(name = "seats")
    private int seats;

    @OneToMany(
            mappedBy = "taxi",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<Booking> taxiBookings;

    public Taxi() {};

    public Taxi(String regNo, int seats) {
        this.regNo = regNo;
        this.seats = seats;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String _regNo) {
        this.regNo = _regNo;
    }

    public void setSeats(short seats) {
        this.seats = seats;
    }

    public int getSeats() {
        return seats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Taxi)) return false;
        Taxi taxi = (Taxi) o;
        if (!regNo.equals(taxi.regNo)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(regNo);
    }

    @Override
    public String toString() {
        return "Taxi{" +
                "id=" + id +
                ", regNo='" + regNo + '\'' +
                ", seats=" + seats +
                '}';
    }
}
