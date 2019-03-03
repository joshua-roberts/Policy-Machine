package gov.nist.csd.pm.common.exceptions;

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

    private int code;
    private String message;

    Errors(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
