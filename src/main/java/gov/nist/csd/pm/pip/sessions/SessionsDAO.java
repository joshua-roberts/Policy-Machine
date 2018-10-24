package gov.nist.csd.pm.pip.sessions;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.SessionDoesNotExistException;

/**
 * Interface methods to manage user sessions in the Policy Machine
 */
public interface SessionsDAO {

    /**
     * Create a new session with the provided ID for the given user.
     * @param sessionID The ID of the session.
     * @param userID The ID of the User to create the session for
     * @throws DatabaseException When there is an error creating a session in the database.
     */
    void createSession(String sessionID, long userID) throws DatabaseException;

    /**
     * Delete a session with the given ID.
     * @param sessionID The ID of the session to delete.
     * @throws DatabaseException When there is an error deleting the session in the database.
     */
    void deleteSession(String sessionID) throws DatabaseException;

    /**
     * Get the User ID associated with the given session ID.
     * @param sessionID The ID of the session to get the User of.
     * @return The ID of the User.
     * @throws SessionDoesNotExistException When the provided session ID does not exist
     */
    long getSessionUserID(String sessionID) throws SessionDoesNotExistException;
}
