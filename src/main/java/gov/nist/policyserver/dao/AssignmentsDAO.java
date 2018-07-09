package gov.nist.policyserver.dao;

import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.model.graph.relationships.Assignment;

import java.util.List;

public interface AssignmentsDAO {

    void createAssignment(long childId, long parentId) throws DatabaseException;

    void deleteAssignment(long childId, long parentId) throws DatabaseException;

    List<Assignment> getAssignments() throws DatabaseException;
}
