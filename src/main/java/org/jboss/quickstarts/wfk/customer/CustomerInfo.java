package org.jboss.quickstarts.wfk.customer;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Embeddable
public class CustomerInfo implements Serializable {
    @NotNull
    @Pattern(regexp = "[A-Za-z-' ]{1,50}", message = "Please use a name without numbers or specials")
    private String name;

    @NotNull
    @NotEmpty
    @Email(message = "The email address must be in the format of name@domain.com")
    private String email;

    @NotNull
    @Pattern(regexp = "^0(\\s*[0-9]\\s*){10}$", message = "Phone number must start with 0 followed by 10 digits." +
            "Spaces in between are allowed.")
    private String phoneNumber;

    public CustomerInfo() {}

    public CustomerInfo(String name, String email, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Copy Constructor. Transfer data from CustomerInfo to a Customer object.
     *
     * @return a Customer object with the same data as the object being called.
     */
    public Customer returnCustomerInstance() {
        return new Customer(this.getName(),this.email,this.phoneNumber);
    }

    @Override
    public String toString() {
        return name + " can be found @ " + email + " or called by at " + phoneNumber;
    }
}
