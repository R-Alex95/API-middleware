package org.jboss.quickstarts.wfk.taxi;

import javax.persistence.Embeddable;
import javax.validation.constraints.*;

@Embeddable
public class TaxiInfo {
    @NotNull
    @Size(min = 1, max = 7)
    @Pattern(regexp = "^([a-zA-Z0-9]){7}$",
            message = "The registration number must be an alphanumeric string of length 7. Spaces not allowed")
    private String regNo;

    @NotNull
    @Min(2)
    @Max(20)
    private int seats;

    public TaxiInfo(){};

    public TaxiInfo(String regNo, int seats) {
        this.regNo = regNo;
        this.seats = seats;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }


    /**
     * Copy Constructor. Transfer data from *this* object to a Taxi object.
     *
     * @return a Taxi object with the same data as *this* object.
     */
    public Taxi returnTaxiInstance() {
        return new Taxi(this.regNo,this.seats);
    }

    @Override
    public String toString() {
        return "TaxiInfo{" +
                "regNo='" + regNo + '\'' +
                ", seats=" + seats +
                '}';
    }
}
