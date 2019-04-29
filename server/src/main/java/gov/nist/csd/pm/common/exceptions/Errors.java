package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.exceptions.PMException;

/**
 * Every error has an error code and general message
 */
public enum Errors {

    // PMRelationException
    ERR_PM(6000, "generic error"),
    ERR_GRAPH(6001, "graph error"),
    ERR_NODE(6002, "node error"),
    ERR_AUTHORIZATION(6003, "authorization error"),
    ERR_AUTHENTICATION(6004, "authentication error"),
    ERR_PROHIBITION(6005, "prohibition error"),
    ERR_DB(6006, "database error"),
    ERR_CONFIG(6007, "configuration error");

    private int    code;
    private String message;

    Errors(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Map an Exception to a specific error.
     * @param e the exception to map to an error.
     * @return the error mapped from the given exception.
     */
    public static Errors toException(PMException e) {
        Errors err;
        if(e instanceof PMAuthenticationException) {
            err = Errors.ERR_AUTHENTICATION;
        } else if(e instanceof PMAuthorizationException) {
            err = Errors.ERR_AUTHORIZATION;
        } else if(e instanceof PMDBException) {
            err = Errors.ERR_DB;
        } else if(e instanceof PMProhibitionException) {
            err = Errors.ERR_PROHIBITION;
        } else if(e instanceof PMGraphException) {
            err = Errors.ERR_GRAPH;
        } else if(e instanceof PMConfigurationException) {
            err = Errors.ERR_CONFIG;
        } else {
            err = Errors.ERR_PM;
        }
        return err;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
