package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.graph.NodeType;
import gov.nist.csd.pm.demos.ndac.translator.exceptions.PMAccessDeniedException;
import gov.nist.csd.pm.pep.response.ApiResponseCodes;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.csd.pm.model.Constants.DESCRIPTION_PROPERTY;
import static gov.nist.csd.pm.model.Constants.PASSWORD_PROPERTY;

public class SessionService extends Service {
    private NodeService      nodeService;
    private AnalyticsService analyticsService;

    public SessionService() {
        nodeService = new NodeService();
        analyticsService = new AnalyticsService();
    }

    public String createSession(String username, String password) throws PmException, InvalidKeySpecException, NoSuchAlgorithmException, IOException, ClassNotFoundException, SQLException {
        //authenticate
        Set<Node> nodes = nodeService.getNodes(username, NodeType.U.toString(), null);
        if (nodes.isEmpty()) {
            throw new NodeNotFoundException(username);
        }

        Node userNode = nodes.iterator().next();

        //check password
        String storedPass = userNode.getProperty(PASSWORD_PROPERTY);

        if (!checkPasswordHash(storedPass, password)) {
            throw new PmException(ApiResponseCodes.ERR_INVALID_CREDENTIALS, "Username and password does not match.");
        }

        //create session id
        String sessionID = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();

        //create session
        getDaoManager().getSessionsDAO().createSession(sessionID, userNode.getID());

        return sessionID;
    }

    public void deleteSession(String sessionID) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        getDaoManager().getSessionsDAO().deleteSession(sessionID);
    }
}
