package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.graph.model.nodes.NodeType;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Set;
import java.util.UUID;

import static gov.nist.csd.pm.common.constants.Properties.PASSWORD_PROPERTY;
import static gov.nist.csd.pm.common.util.NodeUtils.checkPasswordHash;

public class SessionsService extends Service {

    public SessionsService() {}

    /**
     * Given a username and password, check that the user exists and the password matches the one stored for the user.
     * If the user is authenticated, return a new session ID.
     *
     * @param username The name of the user to create a session for.
     * @param password  The password the user provided, to be checked against the password stored for the user.
     * @return the ID of the new session.
     *
     * @throws PMGraphException If a node with the user's name does not exist.
     * @throws PMAuthenticationException If the provided password does not match the stored password.
     * @throws PMGraphException If there is an error hashing the provided user password.
     */
    public String createSession(String username, String password) throws PMException {
        //get the user node
        Set<Node> nodes = getGraphPIP().search(username, NodeType.U.toString(), null);
        if (nodes.isEmpty()) {
            throw new PMGraphException(String.format("node with name %s could not be found", username));
        }

        Node userNode = nodes.iterator().next();

        //check password
        String storedPass = userNode.getProperties().get(PASSWORD_PROPERTY);
        try {
            if (!checkPasswordHash(storedPass, password)) {
                throw new PMAuthenticationException("username or password did not match");
            }
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new PMGraphException(e.getMessage());
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
     * @throws PMConfigurationException if there is an error with the PAP configuration.
     * @throws PMAuthorizationException if the current user is not permitted to perform an action.
     * @throws PMDBException if there is an error accessing the database.
     * @throws PMGraphException if there is an error accessing the graph.
     */
    public void deleteSession(String sessionID) throws PMException {
        getSessionManager().deleteSession(sessionID);
    }
}
