package gov.nist.csd.pm.pip.graph;

import gov.nist.csd.pm.common.exceptions.PMDBException;
import gov.nist.csd.pm.common.exceptions.PMGraphException;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.Graph;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.graph.model.nodes.NodeType;
import gov.nist.csd.pm.graph.model.relationships.Assignment;
import gov.nist.csd.pm.graph.model.relationships.Association;
import gov.nist.csd.pm.pip.db.DatabaseContext;
import gov.nist.csd.pm.pip.db.neo4j.Neo4jConnection;
import gov.nist.csd.pm.pip.db.neo4j.Neo4jHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.csd.pm.pip.db.neo4j.Neo4jHelper.mapToNode;

/**
 * A Neo4j implementation of a NGAC graph
 */
public class Neo4jGraph implements Graph {

    /**
     * Object to store a connection to a Neo4j database.
     */
    private Neo4jConnection neo4j;

    /**
     * Receive context information about the database connection, and create a new connection to the Neo4j instance.
     * @param ctx Context information about the Neo4j connection.
     * @throws PMDBException When there is an error connecting to Neo4j.
     */
    public Neo4jGraph(DatabaseContext ctx) throws PMDBException {
        this.neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());

        // create an index on node IDs
        // this will improve read performance
        String cypher = "create index on :NODE(id)";
        try (
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher)
        ) {
            stmt.executeQuery();
        } catch (SQLException e) {
            throw new PMDBException(e.getMessage());
        }
    }

    /**
     * Create a new node with the information provided in node. The ID is a random long value.
     *
     * @return the ID of the created node.
     * @throws IllegalArgumentException if the ID is 0.
     * @throws IllegalArgumentException if the node name is null or empty.
     * @throws IllegalArgumentException if the node type is null.
     * @throws PMDBException if the provided node is null.
     */
    @Override
    public Node createNode(long id, String name, NodeType nodeType, Map<String, String> properties) throws PMException {
        if (id == 0) {
            throw new IllegalArgumentException("id was 0");
        } else if(name == null || name.isEmpty()) {
            throw new IllegalArgumentException("a null name was provided when creating a new node");
        }else if(nodeType == null) {
            throw new IllegalArgumentException("a null type was provided when creating a new node");
        }

        // if the node properties are null, getPAP to an empty map
        if(properties == null) {
           properties = new HashMap<>();
        }

        // generate a random ID
        String cypher = String.format("create(n:NODE:%s{id: %d, name: '%s', type: '%s'})",
                nodeType, id,name, nodeType);

        // build a string for the node's properties
        StringBuilder propStr = new StringBuilder();
        for (String key : properties.keySet()) {
            if (propStr.length() == 0) {
                propStr.append(String.format("%s: '%s'", key, properties.get(key)));
            }
            else {
                propStr.append(String.format(", %s: '%s'", key, properties.get(key)));
            }
        }
        cypher += String.format(" set n += {%s}", propStr.toString());

        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher)
        ) {
            stmt.executeQuery();
            return new Node(id, name, nodeType, properties);
        } catch (SQLException e) {
            throw new PMDBException(e.getMessage());
        }
    }

    /**
     * Update a node based on the given node context.  Only name and properties can be updated.
     *
     * @throws IllegalArgumentException if the provided node to update is null.
     * @throws IllegalArgumentException if the provided node to update has an ID of 0.
     * @throws PMDBException if there is an error updating the node in Neo4j.
     */
    @Override
    public void updateNode(long id, String name, Map<String, String> properties) throws PMException {
        if(id == 0) {
            //throw an exception if the provided context does not have an ID
            throw new IllegalArgumentException("no ID was provided when updating a node in neo4j");
        }

        Node exNode = getNode(id);

        String cypher = String.format("match(n:NODE:%s{id:%d}) set n={} ", exNode.getType(), id);
        // have to reset the ID etc because neo4j will erase all properties
        // build a string for the node's name and properties
        StringBuilder propStr = new StringBuilder();
        propStr.append(String.format("id: %s", id));
        propStr.append(String.format(", type: '%s'", exNode.getType()));
        if (name != null && !name.isEmpty()) {
            propStr.append(String.format(", name: '%s'", name));
        } else {
            propStr.append(String.format(", name: '%s'", exNode.getName()));
        }

        if (properties != null) {
            for (String key : properties.keySet()) {
                if (propStr.length() == 0) {
                    propStr.append(String.format("%s: '%s'", key, properties.get(key)));
                }
                else {
                    propStr.append(String.format(", %s: '%s'", key, properties.get(key)));
                }
            }
        }

        // if name or properties are being updated, send to Neo4j
        if (propStr.length() > 0) {
            cypher += String.format(" set n += {%s}", propStr.toString());

            try (
                    Connection conn = neo4j.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(cypher)
            ) {
                stmt.executeQuery();
            }
            catch (SQLException e) {
                throw new PMDBException(e.getMessage());
            }
        }
    }

    /**
     * Delete a node from the graph.
     *
     * @throws PMDBException if there is an error deleting the node from the database.
     */
    @Override
    public void deleteNode(long nodeID) throws PMDBException {
        String cypher = String.format("MATCH (n) where n.id=%d DETACH DELETE n", nodeID);
        try (
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher)
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new PMDBException(e.getMessage());
        }
    }

    /**
     * Check if a node with the given ID exists in the database.
     *
     * @return true if a node with the given ID exists, false otherwise.
     * @throws PMDBException if there is an error check if the node exists in the database.
     */
    @Override
    public boolean exists(long nodeID) throws PMDBException {
        String cypher = String.format("match(n{id: %d}) return n", nodeID);
        try (
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            return rs.next();
        }
        catch (SQLException e) {
            throw new PMDBException(e.getMessage());
        }
    }

    /**
     * Retrieve all the nodes from the database.
     *
     * @return the set of all nodes in the database.
     * @throws PMDBException if there is an error retrieving the nodes from the database.
     */
    @Override
    public Collection<Node> getNodes() throws PMDBException {
        String cypher = "match(n:NODE) return n";
        try (
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            HashSet<Node> nodes = new HashSet<>();
            while (rs.next()) {
                HashMap map = (HashMap) rs.getObject(1);
                Node node = mapToNode(map);
                if(node == null) {
                    throw new PMDBException("received a null node from neo4j");
                }

                nodes.add(node);
            }
            return nodes;
        }
        catch (SQLException | PMException e) {
            throw new PMDBException(e.getMessage());
        }
    }

    /**
     * Get the node from the graph with the given ID.
     *
     * @return a Node with the information of the node with the given ID.
     * @throws PMDBException if there is an error retrieving the node from the database.
     * @throws PMGraphException if there is an error converting the data returned from the database into a node.
     */
    @Override
    public Node getNode(long id) throws PMDBException, PMGraphException {
        // get the cypher query
        String cypher = String.format("match(n{id:%d}) return n", id);
        // query neo4j for the nodes
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            if(rs.next()) {
                HashMap map = (HashMap) rs.getObject(1);
                return Neo4jHelper.mapToNode(map);
            }
            throw new PMGraphException(String.format("node with ID %d does not exist", id));
        }
        catch (SQLException e) {
            throw new PMDBException(e.getMessage());
        }
    }

    /**
     * Search the neo4j database for nodes based on name, type, and properties. This implementation does not support
     * wildcard searching.
     * @param name the name of the nodes to search for.
     * @param type the type of the nodes to search for.
     * @param properties the properties of the nodes to search for.
     * @return the set of nodes that match the search parameters.
     * @throws PMDBException if there is an error retrieving the nodes from the database.
     * @throws PMGraphException if there is an error converting the ResultSet to a set of nodes.
     */
    @Override
    public Set<Node> search(String name, String type, Map<String, String> properties) throws PMDBException, PMGraphException {
        // get the cypher query
        String cypher = getSearchCypher(name, type, properties);
        // query neo4j for the nodes
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            return Neo4jHelper.getNodesFromResultSet(rs);
        } catch (SQLException e) {
            throw new PMDBException(e.getMessage());
        }
    }

    private static String getSearchCypher(String name, String type, Map<String,String> properties) {
        String propsStr = "";
        if (name != null && !name.isEmpty()){
            propsStr = String.format("name: \"%s\"", name);
        }

        String typeStr = "";
        if (type != null && !type.isEmpty()){
            typeStr = String.format(":%s", type);
        }

        if (properties != null) {
            for (String key : properties.keySet()) {
                String value = properties.get(key);
                if (propsStr.isEmpty()) {
                    propsStr += String.format("%s: \"%s\"", key, value);
                } else {
                    propsStr += String.format(", %s: \"%s\"", key, value);
                }
            }
        }

        return String.format("match(n%s{%s}) return n", typeStr, propsStr);
    }

    /**
     * Get the policy class nodes in the graph.
     *
     * @return the set of policy class node IDs.
     * @throws PMDBException if there is an error getting the policy classes from the database.
     */
    @Override
    public HashSet<Long> getPolicies() throws PMDBException {
        String cypher = "match(n) where n:PC return n.id";
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            HashSet<Long> nodeIDs = new HashSet<>();
            while (rs.next()) {
                nodeIDs.add(rs.getLong(1));
            }
            return nodeIDs;
        } catch (SQLException e) {
            throw new PMDBException(e.getMessage());
        }
    }

    /**
     * Get the children of the node with the given ID.
     *
     * @return the set of nodes that are assigned to the node with the given ID.
     * @throws PMGraphException if there is an error converting the ResultSet returned from the database to a set of Nodes.
     * @throws PMDBException if there is an error getting the children of the provided node from the database.
     */
    @Override
    public Set<Long> getChildren(long nodeID) throws PMDBException, PMGraphException {
        String cypher = String.format("match(n{id:%d})<-[:assigned_to]-(m) return m.id", nodeID);
        try (
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            Set<Long> children = new HashSet<>();
            while(rs.next()) {
                children.add(rs.getLong(1));
            }
            return children;
        }
        catch (SQLException e) {
            throw new PMDBException(e.getMessage());
        }
    }

    /**
     * Get the parents of the node with the given ID.
     *
     * @return the set of nodes that are assigned to the node with the given ID.
     * @throws PMGraphException if there is an error converting the ResultSet returned from the database to a set of Nodes.
     * @throws PMDBException if there is an error getting the parents of the provided node from the database.
     */
    @Override
    public Set<Long> getParents(long nodeID) throws PMDBException, PMGraphException {
        String cypher = String.format("match(n{id:%d})-[:assigned_to]->(m) return m.id", nodeID);
        try (
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            Set<Long> parents = new HashSet<>();
            while(rs.next()) {
                parents.add(rs.getLong(1));
            }
            return parents;
        }
        catch (SQLException e) {
            throw new PMDBException(e.getMessage());
        }
    }

    /**
     * Assign the child node to the parent node.
     *
     * @throws PMDBException if there is an error assigning the child to the parent in the database.
     * @throws PMGraphException if the child node doesn't exist.
     * @throws PMGraphException if the parent node doesn't exist.
     */
    @Override
    public void assign(long childID, long parentID) throws PMException {
        Node child = getNode(childID);
        Node parent = getNode(parentID);

        Assignment.checkAssignment(child.getType(), parent.getType());

        String cypher = String.format("MATCH (a:%s{id: %d}), (b:%s{id: %d}) " +
                        "CREATE (a)-[:assigned_to]->(b)",child.getType(), child.getID(),
                parent.getType(), parent.getID());
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher)
        ){
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new PMDBException(e.getMessage());
        }
    }

    /**
     * Deassign the child node from the parent node.
     *
     * @throws PMDBException if there is an error deleting this assignment in the database.
     * @throws PMGraphException if the child node doesn't exist.
     * @throws PMGraphException if the parent node doesn't exist.
     */
    @Override
    public void deassign(long childID, long parentID) throws PMException {
        Node child = getNode(childID);
        Node parent = getNode(parentID);

        String cypher = String.format("match (a:%s{id: %d})-[r:assigned_to]->(b:%s{id: %d}) delete r",
                child.getType(), child.getID(), parent.getType(), parent.getID());
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher)
        ){
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new PMDBException(e.getMessage());
        }
    }

    /**
     * Create an association between the user attribute and the target node. If an association already exists, update
     * the operations to the ones provided.
     *
     * @throws PMGraphException if the user attribute node doesn't exist.
     * @throws PMGraphException if the target node doesn't exist.
     * @throws PMDBException if there is an error associating the two nodes in the database.
     */
    @Override
    public void associate(long uaID, long targetID, Set<String> operations) throws PMException {
        Node ua = getNode(uaID);
        Node target = getNode(targetID);

        Association.checkAssociation(ua.getType(), target.getType());

        String operationsStr = Neo4jHelper.setToCypherArray(operations);
        String cypher = String.format("MATCH (ua:UA{id: %d}), (target:%s{id: %d}) " +
                        "merge (ua)-[a:associated_with]->(target) set a.operations = %s", ua.getID(),
                target.getType(), target.getID(), operationsStr);
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher)
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new PMDBException(e.getMessage());
        }
    }

    /**
     * Delete an association between the user attribute and target node.
     *
     * @throws PMGraphException if the user attribute node doesn't exist.
     * @throws PMGraphException if the target node doesn't exist.
     * @throws PMDBException if there is an error dissociating the two nodes in the database.
     */
    @Override
    public void dissociate(long uaID, long targetID) throws PMException {
        Node ua = getNode(uaID);
        Node target = getNode(targetID);

        String cypher = String.format("match (ua:UA{id:%d})-[r:associated_with]->(target%s{id:%d}) delete r",
                ua.getID(), target.getType(), target.getID());
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher)
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new PMDBException(e.getMessage());
        }
    }

    /**
     * Get the associations that the provided node is the source of. Note: Only user attributes can be source nodes in
     * an association.
     *
     * @return a map of target node IDs and operations given to the source node for each association.
     * @throws PMDBException if there is an exception retrieving the associations for the source node in the database.
     */
    @Override
    public Map<Long, Set<String>> getSourceAssociations(long sourceID) throws PMDBException {
        String cypher = String.format("match(source:UA{id:%d})-[a:associated_with]->(target) return target.id, a.operations", sourceID);
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            Map<Long, Set<String>> associations = new HashMap<>();
            while (rs.next()) {
                long targetID = rs.getLong(1);
                HashSet<String> opsSet = new HashSet<>((Collection<String>) rs.getObject(2));
                associations.put(targetID, opsSet);
            }

            return associations;
        }
        catch (SQLException e) {
            throw new PMDBException(e.getMessage());
        }
    }

    /**
     * Get the associations that the provided node is the target of. Note: Only user attributes and Object Attributes
     * can be target nodes in an association.
     *
     * @return a map of source node IDs and operations the source nodes have on the given target ID through each association.
     * @throws PMDBException if there is an exception retrieving the associations for the target node in the database.
     */
    @Override
    public Map<Long, Set<String>> getTargetAssociations(long targetID) throws PMDBException {
        String cypher = String.format("match(source)-[a:associated_with]->(target{id:%d}) return source.id, a.operations", targetID);
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            Map<Long, Set<String>> associations = new HashMap<>();
            while (rs.next()) {
                long sourceID = rs.getLong(1);
                HashSet<String> opsSet = new HashSet<>((Collection) rs.getObject(2));
                associations.put(sourceID, opsSet);
            }

            return associations;
        }
        catch (SQLException e) {
            throw new PMDBException(e.getMessage());
        }
    }
}
