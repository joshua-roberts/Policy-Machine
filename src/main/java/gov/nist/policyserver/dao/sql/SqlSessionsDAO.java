package gov.nist.policyserver.dao.sql;

import gov.nist.policyserver.dao.SessionsDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.SessionDoesNotExistException;

import java.sql.SQLException;

public class SqlSessionsDAO implements SessionsDAO {

    @Override
    public void createSession(String sessionId, long userId) throws DatabaseException, SQLException {

    }

    @Override
    public void deleteSession(String sessionId) {

    }

    @Override
    public long getSessionUserId(String sessionId) throws SessionDoesNotExistException {
        return 0;
    }
}
