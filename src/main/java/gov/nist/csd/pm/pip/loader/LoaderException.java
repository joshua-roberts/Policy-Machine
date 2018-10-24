package gov.nist.csd.pm.pip.loader;

import gov.nist.csd.pm.model.exceptions.PMException;
import gov.nist.csd.pm.model.exceptions.ErrorCodes;

/**
 * Exception to be thrown when there is any error loading a graph into memory.
 */
public class LoaderException extends PMException {
    LoaderException(String message) {
        super(ErrorCodes.ERR_LOADER, message);
    }
}
