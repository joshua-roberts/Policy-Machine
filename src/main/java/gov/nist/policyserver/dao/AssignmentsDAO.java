package gov.nist.policyserver.dao;

import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.model.graph.nodes.Node;

public interface AssignmentsDAO {

    void createAssignment(Node child, Node parent) throws DatabaseException;

    void deleteAssignment(long childId, long parentId) throws DatabaseException;
}
