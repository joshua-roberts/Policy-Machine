package gov.nist.csd.pm.common.model.graph;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.nodes.Node;

import java.util.HashSet;
import java.util.Map;

/**
 * Search provides a method to search for nodes in an NAC graph.
 */
public interface Search {

    /**
     * Search an NGAC graph for nodes matching the given parameters. A node must
     * contain all properties provided to be returned.
     * To get all the nodes that have a specific property key with any value use "*" as the value in the parameter.
     * (i.e. {key=*})
     * @param name The name of the nodes to search for.
     * @param type The type of the nodes to search for.
     * @param properties The properties of the nodes to search for.
     * @return A set of nodes that match the given search criteria.
     */
    HashSet<Node> search(String name, String type, Map<String, String> properties) throws PMException;
    /**
     * Retrieve the node with the given ID.
     * @param id the ID of the node to get.
     * @return The Node with the given ID.
     */
    Node getNode(long id) throws PMException;
}
