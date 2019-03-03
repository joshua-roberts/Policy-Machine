package gov.nist.csd.pm.pap.search;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.model.nodes.Node;

import java.util.Map;
import java.util.Set;

/**
 * Search provides a method to search for nodes in an NAC graph.
 */
public interface Search {

    /**
     * Search an NGAC graph for nodes matching the given parameters. A node must
     * contain all properties provided to be returned.
     * To get all the nodes that have a specific property key with any value use "*" as the value in the parameter.
     * (i.e. {key=*})
     * @param name the name of the nodes to search for.
     * @param type the type of the nodes to search for.
     * @param properties the properties of the nodes to search for.
     * @return a set of nodes that match the given search criteria.
     * @throws PMDBException if a database is being used and there is an error searching for nodes.
     * @throws PMGraphException if there is an error interacting with the graph.
     */
    Set<Node> search(String name, String type, Map<String, String> properties) throws PMDBException, PMGraphException;

    /**
     * Retrieve the node with the given ID.
     * @param id the ID of the node to get.
     * @return the Node with the given ID.
     * @throws PMDBException if a database is being used and there is an error getting the node with the given ID.
     * @throws PMGraphException if there is an error interacting with the graph.
     */
    Node getNode(long id) throws PMException;
}
