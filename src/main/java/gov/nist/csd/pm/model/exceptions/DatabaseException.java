package gov.nist.csd.pm.model.exceptions;

/**
 * This exception will be used for both Neo4j and MySQL.
 */
public class DatabaseException extends PMException {
    public DatabaseException(int code, String msg) {
        super(code, msg);
    }
}
