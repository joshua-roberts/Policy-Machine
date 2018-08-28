package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;
import gov.nist.csd.pm.model.graph.Node;

public class NodeIdExistsException extends PmException {
    public NodeIdExistsException(long id, Node node) {
        super(Constants.ERR_NODE_ID_EXISTS, "A node already exists with ID " + id + ": name=" + node.getName() + ", type=" + node.getType());
    }
}
