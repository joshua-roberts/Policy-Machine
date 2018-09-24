package gov.nist.csd.pm.pip;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.SessionDoesNotExistException;

import java.sql.SQLException;

public interface SessionsDAO {

    void createSession(String sessionId, long userId) throws DatabaseException, SQLException;

    void deleteSession(String sessionId) throws DatabaseException, SQLException;

    long getSessionUserId(String sessionId) throws SessionDoesNotExistException;
}
