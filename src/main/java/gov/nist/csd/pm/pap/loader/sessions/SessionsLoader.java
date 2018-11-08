package gov.nist.csd.pm.pap.loader.sessions;

import gov.nist.csd.pm.model.exceptions.DatabaseException;

import java.util.HashMap;

/**
 * Interface to load sessions from a database to an in memory data structure.
 */
public interface SessionsLoader {

    /**
     * Load any sessions from the database into a map.  The map keys are the session IDs and the values are the User IDs
     * @return The map containing session and User IDs.
     */
    HashMap<String, Long> loadSessions() throws DatabaseException;
}
