package gov.nist.csd.pm.pip.dao;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.SessionDoesNotExistException;

import java.sql.SQLException;

public interface SessionsDAO {

    void createSession(String sessionID, long userID) throws DatabaseException, SQLException;

    void deleteSession(String sessionID) throws DatabaseException, SQLException;

    long getSessionUserID(String sessionID) throws SessionDoesNotExistException;
}
