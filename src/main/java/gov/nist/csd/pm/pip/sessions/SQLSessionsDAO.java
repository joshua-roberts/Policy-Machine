package gov.nist.csd.pm.pip.sessions;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.SessionDoesNotExistException;
import gov.nist.csd.pm.pip.db.sql.SQLConnection;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.util.HashMap;

public class SQLSessionsDAO implements SessionsDAO {
    private HashMap<String, Long> sessions = new HashMap<>();
    private SQLConnection         sql;

    public SQLSessionsDAO(DatabaseContext ctx) throws DatabaseException {
        sql = new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword(), ctx.getSchema());
        loadSessions();
    }

    public void loadSessions() throws DatabaseException {
        sessions.clear();
    }

    @Override
    public void createSession(String sessionID, long userID) throws DatabaseException {

    }

    @Override
    public void deleteSession(String sessionID) throws DatabaseException {

    }

    @Override
    public long getSessionUserID(String sessionID) throws SessionDoesNotExistException {
        return 0;
    }
}
