package gov.nist.csd.pm.model.exceptions;

/**
 * Exception to be thrown when there is any error loading a graph into memory.
 */
public class LoaderException extends PMException {
    public LoaderException(String message) {
        super(ErrorCodes.ERR_LOADER, message);
    }
}
