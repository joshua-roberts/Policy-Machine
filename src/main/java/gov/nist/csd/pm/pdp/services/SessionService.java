package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.graph.NodeType;
import gov.nist.csd.pm.demos.ndac.translator.exceptions.PMAccessDeniedException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static gov.nist.csd.pm.model.Constants.DESCRIPTION_PROPERTY;
import static gov.nist.csd.pm.model.Constants.PASSWORD_PROPERTY;

public class SessionService extends Service {
    private NodeService      nodeService;
    private AnalyticsService analyticsService;

    public SessionService() {
        nodeService = new NodeService();
        analyticsService = new AnalyticsService();
    }

    public String createSession(String username, String password) throws InvalidNodeTypeException, InvalidPropertyException, NodeNotFoundException, PropertyNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException, PMAccessDeniedException, NullNameException, NodeNameExistsException, NodeNameExistsInNamespaceException, ConfigurationException, NullTypeException, NodeIDExistsException, DatabaseException, AssignmentExistsException, InvalidAssignmentException, IOException, ClassNotFoundException, SQLException {
        //authenticate
        HashSet<Node> nodes = nodeService.getNodes(username, NodeType.U.toString(), null);
        if (nodes.isEmpty()) {
            throw new NodeNotFoundException(username);
        }

        Node userNode = nodes.iterator().next();

        //check password
        //get stored password
        String storedPass = userNode.getProperty(PASSWORD_PROPERTY);

        if (!checkPasswordHash(storedPass, password)) {
            throw new PMAccessDeniedException("Username and password does not match.");
        }

        //create session id
        String sessionID = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();

        //create session node
        HashMap<String, String> properties = new HashMap<>();
        properties.put(DESCRIPTION_PROPERTY, "Session for " + username);


        //create session
        getDaoManager().getSessionsDAO().createSession(sessionID, userNode.getID());

        return sessionID;
    }

    public void deleteSession(String sessionID) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        getDaoManager().getSessionsDAO().deleteSession(sessionID);
    }
}
