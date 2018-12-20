package gov.nist.csd.pm.pap.sessions;

import gov.nist.csd.pm.common.exceptions.PMException;

/**
 * Interface methods to manage user sessions in the Policy Machine.  Sessions are not
 * a part of NGAC but are specific to this implementation.
 */
public interface SessionsDAO {

    /**
     * Create a new session with the provided ID for the given user.
     * @param sessionID The ID of the session.
     * @param userID The ID of the User to create the session for
     * @throws PMException When there is an error creating a session in the database.
     */
    void createSession(String sessionID, long userID) throws PMException;

    /**
     * Delete a session with the given ID.
     * @param sessionID The ID of the session to delete.
     * @throws PMException When there is an error deleting the session in the database.
     */
    void deleteSession(String sessionID) throws PMException;

    /**
     * Get the User ID associated with the given session ID.
     * @param sessionID The ID of the session to get the User of.
     * @return The ID of the User.
     * @throws PMException When the provided session ID does not exist
     */
    long getSessionUserID(String sessionID) throws PMException;
}
