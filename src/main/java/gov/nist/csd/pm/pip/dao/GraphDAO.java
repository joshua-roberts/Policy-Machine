package gov.nist.csd.pm.pip.dao;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidNodeTypeException;
import gov.nist.csd.pm.model.exceptions.InvalidPropertyException;
import gov.nist.csd.pm.model.graph.Assignment;
import gov.nist.csd.pm.model.graph.Association;
import gov.nist.csd.pm.pdp.analytics.PmAnalytics;
import gov.nist.csd.pm.pip.graph.PmGraph;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.prohibitions.Prohibition;

import java.io.IOException;
import java.util.List;

public interface GraphDAO {

    PmGraph buildGraph() throws DatabaseException, IOException, ClassNotFoundException, InvalidPropertyException;

    List<Node> getNodes() throws DatabaseException, InvalidPropertyException, InvalidNodeTypeException;

    List<Assignment> getAssignments() throws DatabaseException, InvalidNodeTypeException;

    List<Association> getAssociations() throws DatabaseException, InvalidNodeTypeException;

    PmGraph getGraph();

    PmAnalytics getAnalytics();

    void reset() throws DatabaseException;

    List<Prohibition> getProhibitions() throws DatabaseException;
}
