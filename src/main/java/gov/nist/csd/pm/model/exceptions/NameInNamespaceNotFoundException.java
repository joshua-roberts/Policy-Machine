package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.graph.NodeType;
import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class NameInNamespaceNotFoundException extends PmException {
    public NameInNamespaceNotFoundException(String namespace, String nodeName, NodeType type) {
        super(ApiResponseCodes.ERR_NAME_IN_NAMESPACE_NOT_FOUND, String.format("Node with name '%s' and type %s does not " +
                "exist in namespace %s", nodeName, namespace, type.toString()));
    }
}
