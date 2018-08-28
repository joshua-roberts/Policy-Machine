package gov.nist.csd.pm.pip;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.graph.Node;

public interface AssignmentsDAO {

    void createAssignment(Node child, Node parent) throws DatabaseException;

    void deleteAssignment(long childId, long parentId) throws DatabaseException;
}
