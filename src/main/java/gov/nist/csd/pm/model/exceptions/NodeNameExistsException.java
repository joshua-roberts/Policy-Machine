package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class NodeNameExistsException extends PmException {
    public NodeNameExistsException(String name) {
        super(ApiResponseCodes.ERR_NODE_NAME_EXISTS, String.format("A node with the name '%s' already exists", name));
    }
}
