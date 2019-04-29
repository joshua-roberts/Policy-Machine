package gov.nist.csd.pm.pep.sessions;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to store active sessions in memory.  Since sessions are not persisted if the server shuts down, there is no need
 * to store them in the database.
 */
public class Sessions {

    private static Sessions sess;

    public static Sessions getSessions() {
        if(sess == null) {
            sess = new Sessions();
        }

        return sess;
    }

    private HashMap<String, Long> sessions;

    public Sessions() {
        sessions = new HashMap<>();
    }

    public void create(String sessionID, long userID) {
        sessions.put(sessionID, userID);
    }

    public long get(String sessionID) {
        return sessions.get(sessionID);
    }

    public void delete(String sessionID) {
        sessions.remove(sessionID);
    }

    public Map<String, Long> getAll() {
        return sessions;
    }

    public void reset() {
        sessions.clear();
    }
}
