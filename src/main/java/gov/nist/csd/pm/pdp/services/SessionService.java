package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.OldNode;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pep.response.ApiResponseCodes;
import gov.nist.csd.pm.pip.loader.LoaderException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.csd.pm.model.Constants.PASSWORD_PROPERTY;
import static gov.nist.csd.pm.pip.PIP.getPIP;

public class SessionService extends Service {
    private NodeService      nodeService;

    public SessionService(String sessionID, long processID) {
        super(sessionID, processID);
        nodeService = new NodeService(sessionID, processID);
    }

    public String createSession(String username, String password) throws PMException, InvalidKeySpecException, NoSuchAlgorithmException, IOException, ClassNotFoundException, SQLException {
        //authenticate
        Set<OldNode> nodes = nodeService.getNodes(username, NodeType.U.toString(), null);
        if (nodes.isEmpty()) {
            throw new NodeNotFoundException(username);
        }

        OldNode userNode = nodes.iterator().next();

        //check password
        String storedPass = userNode.getProperty(PASSWORD_PROPERTY);

        if (!checkPasswordHash(storedPass, password)) {
            throw new PMException(ApiResponseCodes.ERR_INVALID_CREDENTIALS, "Username and password does not match.");
        }

        //create session id
        String sessionID = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();

        //create session
        getPIP().getSessionsDAO().createSession(sessionID, userNode.getID());

        return sessionID;
    }

    public void deleteSession(String sessionID) throws DatabaseException, IOException, ClassNotFoundException, LoaderException {
        getPIP().getSessionsDAO().deleteSession(sessionID);
    }
}
