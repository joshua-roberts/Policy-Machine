package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class NodeIDExistsException extends PmException {
    public NodeIDExistsException(long id, Node node) {
        super(ApiResponseCodes.ERR_NODE_ID_EXISTS, "A node already exists with ID " + id + ": name=" + node.getName() + ", type=" + node.getType());
    }
}
