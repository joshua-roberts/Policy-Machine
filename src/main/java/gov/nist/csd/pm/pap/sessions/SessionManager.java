package gov.nist.csd.pm.pap.sessions;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to store active sessions in memory.  Since sessions are not persisted if the server shuts down, there is no need
 * to store them in the database.
 */
public class SessionManager {

    private HashMap<String, Long> sessions;

    public SessionManager() {
        sessions = new HashMap<>();
    }

    public void createSession(String sessionID, long userID) {
        sessions.put(sessionID, userID);
    }

    public long getSessionUserID(String sessionID) {
        return sessions.get(sessionID);
    }

    public void deleteSession(String sessionID) {
        sessions.remove(sessionID);
    }

    public Map<String, Long> getSessions() {
        return sessions;
    }

    public void reset() {
        sessions.clear();
    }
}
