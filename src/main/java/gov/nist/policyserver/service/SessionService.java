package gov.nist.policyserver.service;

import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.nodes.Property;
import gov.nist.policyserver.translator.exceptions.PMAccessDeniedException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.UUID;

import static gov.nist.policyserver.common.Constants.DESCRIPTION_PROPERTY;
import static gov.nist.policyserver.common.Constants.PASSWORD_PROPERTY;

public class SessionService extends Service {
    private NodeService      nodeService;
    private AnalyticsService analyticsService;

    public SessionService() {
        nodeService = new NodeService();
        analyticsService = new AnalyticsService();
    }

    public String createSession(String username, String password) throws InvalidNodeTypeException, InvalidPropertyException, NodeNotFoundException, PropertyNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException, PMAccessDeniedException, NullNameException, NodeNameExistsException, NodeNameExistsInNamespaceException, ConfigurationException, NullTypeException, NodeIdExistsException, DatabaseException, AssignmentExistsException, InvalidAssignmentException, IOException, ClassNotFoundException, SQLException {
        //authenticate
        HashSet<Node> nodes = nodeService.getNodes(null, username, NodeType.USER.toString(), null);
        if (nodes.isEmpty()) {
            throw new NodeNotFoundException(username);
        }

        Node userNode = nodes.iterator().next();

        //check password
        //get stored password
        Property property = userNode.getProperty(PASSWORD_PROPERTY);
        String storedPass = property.getValue();

        if (!checkPasswordHash(storedPass, password)) {
            throw new PMAccessDeniedException("Username and password does not match.");
        }

        //create session id
        String sessionId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();

        //create session node
        Property[] properties = new Property[]{
                new Property(DESCRIPTION_PROPERTY, "Session for " + username)
        };


        //create session
        getDaoManager().getSessionsDAO().createSession(sessionId, userNode.getId());

        return sessionId;
    }

    public void deleteSession(String sessionId) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        getDaoManager().getSessionsDAO().deleteSession(sessionId);
    }
}
