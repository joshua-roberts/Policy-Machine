package gov.nist.csd.pm.pap.loader.sessions;

import gov.nist.csd.pm.model.exceptions.DatabaseException;

import java.util.HashMap;

/**
 * A dummy implementation of the SessionLoader interface.
 */
public class DummySessionsLoader implements SessionsLoader {
    @Override
    public HashMap<String, Long> loadSessions() throws DatabaseException {
        return new HashMap<>();
    }
}
