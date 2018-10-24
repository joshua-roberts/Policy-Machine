package gov.nist.csd.pm.pip.sessions;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.SessionDoesNotExistException;

import java.util.HashMap;

/**
 * Interface methods to manage user sessions in the Policy Machine
 */
public abstract class SessionsDAO {

    /**
     * HashMap to store session and User IDs
     */
    HashMap<String, Long> sessions = new HashMap<>();

    /**
     * Load any existing sessions from the database into the HashMap
     */
    abstract void loadSessions() throws DatabaseException;

    /**
     * Create a new session with the provided ID for the given user.
     * @param sessionID The ID of the session.
     * @param userID The ID of the User to create the session for
     * @throws DatabaseException When there is an error creating a session in the database.
     */
    abstract void createSession(String sessionID, long userID) throws DatabaseException;

    /**
     * Delete a session with the given ID.
     * @param sessionID The ID of the session to delete.
     * @throws DatabaseException When there is an error deleting the session in the database.
     */
    abstract void deleteSession(String sessionID) throws DatabaseException;

    /**
     * Get the User ID associated with the given session ID.
     * @param sessionID The ID of the session to get the User of.
     * @return The ID of the User.
     * @throws SessionDoesNotExistException When the provided session ID does not exist
     */
    public long getSessionUserID(String sessionID) throws SessionDoesNotExistException {
        Long nodeId = sessions.get(sessionID);
        if(nodeId == null) {
            throw new SessionDoesNotExistException(sessionID);
        }

        return nodeId;
    }
}
