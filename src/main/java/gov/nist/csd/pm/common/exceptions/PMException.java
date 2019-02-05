package gov.nist.csd.pm.common.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = {"stackTrace", "cause", "localizedMessage", "suppressed"})
public class PMException extends Exception {
    private static final long serialVersionUID           = 1L;

    private Errors error;
    private String detailedMessage;

    public PMException(Errors error, String msg) {
        super("(code=" + error.getCode() + ") " + msg);
        this.error = error;
        this.detailedMessage = msg;
    }

    public Errors getError() {
        return error;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }
}
