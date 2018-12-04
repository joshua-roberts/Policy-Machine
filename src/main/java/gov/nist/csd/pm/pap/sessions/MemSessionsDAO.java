package gov.nist.csd.pm.pap.sessions;

import gov.nist.csd.pm.common.exceptions.DatabaseException;
import gov.nist.csd.pm.pap.loader.sessions.SessionsLoader;

import java.util.HashMap;

public class MemSessionsDAO implements SessionsDAO {

    /**
     * HashMap to store session and User IDs.
     */
    protected HashMap<String, Long> sessions;

    public MemSessionsDAO(SessionsLoader loader) throws DatabaseException {
        sessions = loader.loadSessions();
    }

    @Override
    public void createSession(String sessionID, long userID) {
        sessions.put(sessionID, userID);
    }

    @Override
    public void deleteSession(String sessionID) {
        sessions.remove(sessionID);
    }

    @Override
    public long getSessionUserID(String sessionID) {
        return sessions.get(sessionID);
    }
}
