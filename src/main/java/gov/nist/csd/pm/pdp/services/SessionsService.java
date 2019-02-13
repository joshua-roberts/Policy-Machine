package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.common.exceptions.Errors;
import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static gov.nist.csd.pm.common.constants.Properties.PASSWORD_PROPERTY;
import static gov.nist.csd.pm.common.model.graph.nodes.NodeUtils.checkPasswordHash;

public class SessionsService extends Service {

    public SessionsService() {}

    /**
     * Given a username and password, check that the user exists and the password matches the one stored for the user.
     * If the user is authenticated, return a new session ID.
     *
     * @param username The name of the user to create a session for.
     * @param password  The password the user provided, to be checked against the password stored for the user.
     * @return The ID of the new session.
     *
     * @throws PMException If a node with the user's name does not exist.
     * @throws PMException If the provided password does not match the stored password.
     * @throws PMException If there is an error hashing the provided password.
     */
    public String createSession(String username, String password) throws PMException {
        //get the user node
        HashSet<NodeContext> nodes = getGraphPAP().search(username, NodeType.U.toString(), null);
        if (nodes.isEmpty()) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node with name %s could not be found", username));
        }

        NodeContext userNode = nodes.iterator().next();

        //check password
        String storedPass = userNode.getProperties().get(PASSWORD_PROPERTY);
        try {
            if (!checkPasswordHash(storedPass, password)) {
                throw new PMException(Errors.ERR_INVALID_CREDENTIALS, "username or password did not match");
            }
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new PMException(Errors.ERR_HASHING_USER_PSWD, e.getMessage());
        }

        //create session id
        String sessionID = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();

        //create session in the PAP
        getSessionManager().createSession(sessionID, userNode.getID());

        return sessionID;
    }

    public long getSessionUserID(String sessionID) throws PMException {
        return getSessionManager().getSessionUserID(sessionID);
    }

    /**
     * Delete the session with the given ID.
     * @param sessionID The ID of the session to delete.
     * @throws PMException If there is an error deleting the session in the session manager
     */
    public void deleteSession(String sessionID) throws PMException {
        getSessionManager().deleteSession(sessionID);
    }
}
