package gov.nist.csd.pm.common.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = {"stackTrace", "cause", "localizedMessage", "suppressed"})
public class PMException extends Exception {
    private static final long serialVersionUID           = 1L;

    private int code;

    public PMException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public PMException(int code) {
        super();
        this.code = code;
    }

    public int getErrorCode() {
        return code;
    }
}
