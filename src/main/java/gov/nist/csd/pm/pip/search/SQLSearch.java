package gov.nist.csd.pm.pip.search;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidNodeTypeException;
import gov.nist.csd.pm.model.exceptions.NodeNotFoundException;
import gov.nist.csd.pm.model.graph.Search;
import gov.nist.csd.pm.model.graph.nodes.Node;

import java.util.HashSet;
import java.util.Map;

/**
 * Implementation of the Search interface using MySQL
 */
public class SQLSearch implements Search {

    @Override
    public HashSet<Node> search(String name, String type, Map<String, String> properties) throws DatabaseException {
        return null;
    }

    @Override
    public Node getNode(long id) throws NodeNotFoundException, DatabaseException, InvalidNodeTypeException {
        return null;
    }
}
