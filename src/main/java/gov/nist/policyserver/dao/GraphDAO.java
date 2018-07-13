package gov.nist.policyserver.dao;

import gov.nist.policyserver.analytics.PmAnalytics;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.InvalidNodeTypeException;
import gov.nist.policyserver.exceptions.InvalidPropertyException;
import gov.nist.policyserver.graph.PmGraph;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.relationships.Assignment;
import gov.nist.policyserver.model.graph.relationships.Association;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface GraphDAO {

    PmGraph buildGraph() throws DatabaseException, IOException, ClassNotFoundException, InvalidPropertyException;

    List<Node> getNodes() throws DatabaseException, InvalidPropertyException, InvalidNodeTypeException;

    List<Assignment> getAssignments() throws DatabaseException, InvalidNodeTypeException;

    List<Association> getAssociations() throws DatabaseException, InvalidNodeTypeException;

    PmGraph getGraph();

    PmAnalytics getAnalytics();

    void reset() throws DatabaseException;
}
