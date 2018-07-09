package gov.nist.policyserver.dao;

import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.SessionDoesNotExistException;

import java.sql.SQLException;

public interface SessionsDAO {

    void createSession(String sessionId, long userId) throws DatabaseException, SQLException;

    void deleteSession(String sessionId) throws DatabaseException, SQLException;

    long getSessionUserId(String sessionId) throws SessionDoesNotExistException;
}
