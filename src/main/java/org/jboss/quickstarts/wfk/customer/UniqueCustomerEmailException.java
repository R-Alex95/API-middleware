package org.jboss.quickstarts.wfk.customer;

import javax.validation.ValidationException;

public class UniqueCustomerEmailException extends ValidationException {

    public UniqueCustomerEmailException(String message) {
        super(message);
    }

    public UniqueCustomerEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniqueCustomerEmailException(Throwable cause) {
        super(cause);
    }
}
