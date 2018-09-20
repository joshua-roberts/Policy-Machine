package gov.nist.csd.pm.pip.dao;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.graph.Node;

public interface AssignmentsDAO {

    void createAssignment(Node child, Node parent) throws DatabaseException;

    void deleteAssignment(long childID, long parentID) throws DatabaseException;
}
