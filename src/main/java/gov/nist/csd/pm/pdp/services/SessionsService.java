package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.model.exceptions.LoaderException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashSet;
import java.util.UUID;

import static gov.nist.csd.pm.model.constants.Properties.PASSWORD_PROPERTY;

public class SessionsService extends Service {

    /**
     * Call to Service constructor with a sessionID and processID.  The process ID can be 0 and will be 0.  Since the
     * Service constructor throws an exception for a null or empty session ID, pass a dummy session ID to avoid the exception
     * being thrown.
     */
    public SessionsService() {
        super("dummy_session_id", 0);
    }

    /**
     * Given a username and password, check that the user exists and the password matches the one stored for the user.
     * If the user is authenticated, return a new session ID.
     * @param username The name of the user to create a session for.
     * @param password  The password the user provided, to be checked against the password stored for the user.
     * @return The ID of the new session.
     */
    public String createSession(String username, String password) throws NodeNotFoundException, DatabaseException, LoaderException, SessionDoesNotExistException, LoadConfigException, MissingPermissionException, HashingUserPasswordException, PMAuthenticationException, InvalidProhibitionSubjectTypeException {
        //get the user node
        HashSet<Node> nodes = getSearch().search(username, NodeType.U.toString(), null);
        if (nodes.isEmpty()) {
            throw new NodeNotFoundException(username);
        }

        Node userNode = nodes.iterator().next();

        //check password
        //get stored password
        String storedPass = userNode.getProperties().get(PASSWORD_PROPERTY);
        try {
            if (!checkPasswordHash(storedPass, password)) {
                throw new PMAuthenticationException();
            }
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new HashingUserPasswordException();
        }

        //create session id
        String sessionID = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();

        //create session in the PAP
        getSessionsDB().createSession(sessionID, userNode.getID());
        getSessionsMem().createSession(sessionID, userNode.getID());

        return sessionID;
    }

    /**
     * Delete the session with the given ID.
     * @param sessionID The ID of the session to delete.
     */
    public void deleteSession(String sessionID) throws LoadConfigException, DatabaseException, LoaderException, InvalidProhibitionSubjectTypeException {
        getSessionsDB().deleteSession(sessionID);
        getSessionsMem().deleteSession(sessionID);
    }
}
