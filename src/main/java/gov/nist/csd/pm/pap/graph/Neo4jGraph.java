package gov.nist.csd.pm.pap.graph;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Graph;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.exceptions.ErrorCodes;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pap.db.neo4j.Neo4jConnection;
import gov.nist.csd.pm.pap.db.neo4j.Neo4jHelper;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.loader.graph.Neo4jGraphLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.csd.pm.model.constants.Properties.NAMESPACE_PROPERTY;
import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_DB;
import static gov.nist.csd.pm.pap.db.neo4j.Neo4jHelper.mapToNode;

/**
 * A Neo4j implementation of a NGAC graph
 */
public class Neo4jGraph implements Graph {

    /**
     * Object to store a connection to a Neo4j database.
     */
    private Neo4jConnection neo4j;
    private DatabaseContext dbCtx;

    /**
     * Receive context information about the database connection, and create a new connection to the Neo4j instance.
     * @param ctx Context information about the Neo4j connection.
     * @throws DatabaseException When there is an error connecting to the instance.
     */
    public Neo4jGraph(DatabaseContext ctx) throws DatabaseException {
        this.dbCtx = ctx;
        this.neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());

        // create an index on node IDs
        // this will improve read performance
        String cypher = "create index on :NODE(id)";
        try (
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher)
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    /**
     * Create a new node with the information provided in node. The ID is a random long value.
     *
     * @param node The context of the node to create.  This includes the id, name, type, and properties.
     * @return The newly created node with it's ID.
     * @throws NullNodeException If the provided Node to create is null.
     * @throws DatabaseException If there is an error creating the node in the database.
     */
    @Override
    public long createNode(Node node) throws NullNodeException, DatabaseException {
        if (node == null) {
            throw new NullNodeException();
        }

        long id = new Random().nextLong();
        String cypher = String.format("create(n:NODE:%s{id: %d, name: '%s', type: '%s'})", node.getType(), id, node.getName(), node
                .getType());

        // build a string for the node's properties
        StringBuilder propStr = new StringBuilder();
        for (String key : node.getProperties().keySet()) {
            if (propStr.length() == 0) {
                propStr.append(String.format("%s: '%s'", key, node.getProperties().get(key)));
            }
            else {
                propStr.append(String.format(", %s: '%s'", key, node.getProperties().get(key)));
            }
        }
        cypher += String.format(" set n += {%s}", propStr.toString());

        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
        ) {
            stmt.executeQuery();
            //return the node with the hashed ID
            return id;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, "error returning the new node's ID: " + e.getMessage());
        }
    }

    /**
     * Update a node based on the given node context.  Only name and properties can be updated.
     * cannot be updated
     * @param node The context of the node to update. This includes the id, name, type, and properties.
     * @throws NullNodeException If the provided Node to update is null.
     * @throws NoIDException If the provided Node object does not have an ID.
     * @throws DatabaseException If there is an error updating the node in the database.
     */
    @Override
    public void updateNode(Node node) throws NullNodeException, NoIDException, DatabaseException {
        if(node == null) {
            throw new NullNodeException();
        } else if(node.getID() == 0) {
            //throw an exception if the provided context does not have an ID
            throw new NoIDException();
        }

        String cypher = String.format("match(n:NODE{id:%d})", node.getID());

        // build a string for the node's name and properties
        StringBuilder namePropsStr = new StringBuilder();
        if (node.getName() != null && !node.getName().isEmpty()) {
            namePropsStr.append(String.format("name: '%s'", node.getName()));
        }

        if (node.getProperties() != null) {
            for (String key : node.getProperties().keySet()) {
                if (namePropsStr.length() == 0) {
                    namePropsStr.append(String.format("%s: '%s'", key, node.getProperties().get(key)));
                }
                else {
                    namePropsStr.append(String.format(", %s: '%s'", key, node.getProperties().get(key)));
                }
            }
        }

        // if name or properties are being updated, send to Neo4j
        if (namePropsStr.length() > 0) {
            cypher += String.format(" set n += {%s} return n.id", namePropsStr.toString());

            try (
                    Connection conn = neo4j.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(cypher)
            ) {
                stmt.executeQuery();
            }
            catch (SQLException e) {
                throw new DatabaseException(ERR_DB, e.getMessage());
            }
        }
    }

    /**
     * Delete a node from the graph.
     *
     * @param nodeID the ID of the node to delete.
     * @throws DatabaseException If there is an error deleting the node from the database.
     */
    @Override
    public void deleteNode(long nodeID) throws DatabaseException {
        String cypher = String.format("MATCH (n) where n.id=%d DETACH DELETE n", nodeID);
        try (
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    /**
     * Check if a node with the given ID exists in the database.
     *
     * @param nodeID the ID of the node to check for.
     * @return True if a node with the given ID exists, false otherwise.
     * @throws DatabaseException If there is an error check if the node exists in the database.
     */
    @Override
    public boolean exists(long nodeID) throws DatabaseException {
        String cypher = "match(n{id: %d}) return n";
        try (
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            return rs.next();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    /**
     * Retreive all the nodes from the database.
     *
     * @return The set of all nodes in the database.
     * @throws DatabaseException If there is an error retrieving the nodes from the database.
     */
    @Override
    public HashSet<Node> getNodes() throws DatabaseException {
        String cypher = "match(n:NODE) return n";
        try (
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            HashSet<Node> nodes = new HashSet<>();
            while (rs.next()) {
                LinkedHashMap map = (LinkedHashMap) rs.getObject(1);
                Node node = mapToNode(map);
                nodes.add(node);
            }
            return nodes;
        }
        catch (SQLException | InvalidNodeTypeException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    /**
     * Get the Policy Class nodes in the graph.
     *
     * @return The set of Policy Class node IDs.
     * @throws DatabaseException If there is an error getting the policy classes from the database.
     */
    @Override
    public HashSet<Long> getPolicies() throws DatabaseException {
        return new Neo4jGraphLoader(dbCtx).getPolicies();
    }

    /**
     * Get the children of the node with the given ID.
     *
     * @param nodeID The ID of the node to get the children of.
     * @return The set of nodes that are assigned to the node with the given ID.
     * @throws DatabaseException If there is an error getting the children of the provided node.
     */
    @Override
    public HashSet<Node> getChildren(long nodeID) throws DatabaseException {
        String cypher = String.format("match(n{id:%d})<-[:assigned_to]-(m) return m", nodeID);
        try (
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            return Neo4jHelper.getNodesFromResultSet(rs);
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    /**
     * Get the parents of the node with the given ID.
     *
     * @param nodeID The ID of the node to get the children of.
     * @return The set of nodes that are assigned to the node with the given ID.
     * @throws DatabaseException If there is an error getting the children of the provided node.
     */
    @Override
    public HashSet<Node> getParents(long nodeID) throws DatabaseException {
        String cypher = String.format("match(n{id:%d})-[:assigned_to]->(m) return m", nodeID);
        try (
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            return Neo4jHelper.getNodesFromResultSet(rs);
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    /**
     * Assign the child node to the parent node.
     *
     * @param childID The ID of the child node.
     * @param childType The type of the child node.
     * @param parentID The the ID of the parent node.
     * @param parentType The type of the parent node.
     * @throws DatabaseException If there is an error assigning the child to the parent in the database.
     */
    @Override
    public void assign(long childID, NodeType childType, long parentID, NodeType parentType) throws DatabaseException {
        String cypher = String.format("MATCH (a:%s{id: %d}), (b:%s{id: %d}) " +
                "CREATE (a)-[:assigned_to]->(b)", childType, childID, parentType, parentID);
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher)
        ){
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ErrorCodes.ERR_DB, e.getMessage());
        }
    }

    /**
     * Deassign the child node from the parent node.
     * @param childID The ID of the child node.
     * @param childType The type of the child node.
     * @param parentID The the ID of the parent node.
     * @param parentType The type of the parent node.
     * @throws DatabaseException If there is an error deleting this assignment in the database.
     */
    @Override
    public void deassign(long childID, NodeType childType, long parentID, NodeType parentType) throws DatabaseException {
        String cypher = String.format("match (a:%s{id: %d})-[r:assigned_to]->(b:%s{id: %d}) delete r", childType, childID, parentType, parentID);
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher)
        ){
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ErrorCodes.ERR_DB, e.getMessage());
        }
    }

    /**
     * Create an association between the User Attribute and the target node. If an associatino already exists, update
     * the operations to the ones provided.
     * @param uaID The ID of the user Attribute.
     * @param targetID The ID of the target node.
     * @param targetType The type of the target node.
     * @param operations A Set of operations to add to the Association.
     * @throws DatabaseException If there is an error associating the two nodes in the database.
     */
    @Override
    public void associate(long uaID, long targetID, NodeType targetType, HashSet<String> operations) throws DatabaseException {
        String operationsStr = Neo4jHelper.setToCypherArray(operations);
        String cypher = String.format("MATCH (ua:UA{id: %d}), (target:%s{id: %d}) " +
                "merge (ua)-[a:associated_with]->(target) set a.operations = %s", uaID, targetType, targetID, operationsStr);
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher)
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    /**
     * Delete an association between the User Attribute and target node.
     * @param uaID The ID of the User Attribute.
     * @param targetID The ID of the target node.
     * @param targetType The type of the target node.
     * @throws DatabaseException If there is an error deleting the association in the database.
     */
    @Override
    public void dissociate(long uaID, long targetID, NodeType targetType) throws DatabaseException {
        String cypher = String.format("match (ua:UA{id:%d})-[r:associated_with]->(target:%s{id:%d}) delete r", uaID, targetType, targetID);
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    /**
     * Get the associations that the provided node is the source of. Note: Only User Attributes can be source nodes in
     * an association.
     *
     * @param sourceID The ID of the source node.
     * @return A map of target node IDs and operations given to the source node for each association.
     * @throws DatabaseException If there is an exception retrieving the associations for the source node in the database.
     */
    @Override
    public HashMap<Long, HashSet<String>> getSourceAssociations(long sourceID) throws DatabaseException {
        String cypher = String.format("match(source:UA{id:%d})-[a:associated_with]->(target) return target.id, a.operations", sourceID);
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            HashMap<Long, HashSet<String>> associations = new HashMap<>();
            while (rs.next()) {
                long targetID = rs.getLong(1);
                //get operations as json
                String opsStr = rs.getString(2);
                //convert ops json to hashset
                HashSet<String> opsSet = Neo4jHelper.getStringSetFromJson(opsStr);

                associations.put(targetID, opsSet);
            }

            return associations;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    /**
     * Get the associations that the provided node is the target of. Note: Only User Attributes and Object Attributes
     * can be target nodes in an association.
     *
     * @param targetID the ID of the target node.
     * @return A map of source node IDs and operations the source nodes have on the given target ID through each association.
     * @throws DatabaseException If there is an exception retrieving the associations for the target node in the database.
     */
    @Override
    public HashMap<Long, HashSet<String>> getTargetAssociations(long targetID) throws DatabaseException {
        String cypher = String.format("match(source)<-[a:associated_with]-(target{id:%d}) return target.id, a.operations", targetID);
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            HashMap<Long, HashSet<String>> associations = new HashMap<>();
            while (rs.next()) {
                //get operations as json
                String opsStr = rs.getString(2);
                //convert ops json to hashset
                HashSet<String> opsSet = Neo4jHelper.getStringSetFromJson(opsStr);

                associations.put(targetID, opsSet);
            }

            return associations;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }
}
