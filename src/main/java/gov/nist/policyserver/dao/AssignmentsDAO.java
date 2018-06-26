package gov.nist.policyserver.dao;

import gov.nist.policyserver.exceptions.DatabaseException;

import java.sql.Connection;

public interface AssignmentsDAO {

    void createAssignment(long childId, long parentId) throws DatabaseException;

    void deleteAssignment(long childId, long parentId) throws DatabaseException;
}
