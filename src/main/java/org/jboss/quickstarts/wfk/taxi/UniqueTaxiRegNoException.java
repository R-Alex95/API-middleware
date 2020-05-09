package org.jboss.quickstarts.wfk.taxi;

import javax.validation.ValidationException;

public class UniqueTaxiRegNoException extends ValidationException {

    public UniqueTaxiRegNoException(String message) {
        super(message);
    }

    public UniqueTaxiRegNoException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniqueTaxiRegNoException(Throwable cause) {
        super(cause);
    }
}
