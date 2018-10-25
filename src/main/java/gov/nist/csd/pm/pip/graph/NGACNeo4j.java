package gov.nist.csd.pm.pip.graph;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.NGAC;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.exceptions.ErrorCodes;
import gov.nist.csd.pm.pip.db.neo4j.Neo4jConnection;
import gov.nist.csd.pm.pip.db.neo4j.Neo4jHelper;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_DB;

/**
 * A Neo4j implementation of a NGAC database
 */
public class NGACNeo4j implements NGAC {

    /**
     * Object to store a connection to a Neo4j database.
     */
    private Neo4jConnection neo4j;

    /**
     * Receive context information about the database connection, and create a new connection to the Neo4j instance.
     * @param ctx Context information about the Neo4j connection.
     * @throws DatabaseException When there is an error connecting to the instance.
     */
    public NGACNeo4j(DatabaseContext ctx) throws DatabaseException {
        neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
    }

    @Override
    public Node createNode(Node ctx) throws NullNodeCtxException, DatabaseException {
        if (ctx == null) {
            throw new NullNodeCtxException();
        }

        String cypher = String.format("match(n) with max(n.id) as ID create (n:NODE:%s{id: ID+1, name: '%s', type: '%s'})", ctx.getType(), ctx.getName(), ctx.getType());

        // build a string for the node's properties
        StringBuilder propStr = new StringBuilder();
        for (String key : ctx.getProperties().keySet()) {
            if (propStr.length() == 0) {
                propStr.append(String.format("%s: '%s'", key, ctx.getProperties().get(key)));
            }
            else {
                propStr.append(String.format(", %s: '%s'", key, ctx.getProperties().get(key)));
            }
        }
        cypher += String.format(" set n += {%s} return n.id", propStr.toString());

        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            // get the ID that is returned from executing the cypher
            rs.next();
            long id = rs.getLong(1);
            return new Node()
                    .id(id)
                    .name(ctx.getName())
                    .type(ctx.getType())
                    .properties(ctx.getProperties());
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, "error returning the new node's ID: " + e.getMessage());
        }
    }

    @Override
    public void updateNode(Node ctx) throws NullNodeCtxException, NoIDException, DatabaseException {
        if(ctx == null) {
            throw new NullNodeCtxException();
        } else if(ctx.getID() == 0) {
            //throw an exception if the provided context does not have an ID
            throw new NoIDException();
        }

        String cypher = String.format("match(n:NODE{id:%d})", ctx.getID());

        // build a string for the node's name and properties
        StringBuilder namePropsStr = new StringBuilder();
        if (ctx.getName() != null && !ctx.getName().isEmpty()) {
            namePropsStr.append(String.format("name: '%s'", ctx.getName()));
        }

        if (ctx.getProperties() != null) {
            for (String key : ctx.getProperties().keySet()) {
                if (namePropsStr.length() == 0) {
                    namePropsStr.append(String.format("%s: '%s'", key, ctx.getProperties().get(key)));
                }
                else {
                    namePropsStr.append(String.format(", %s: '%s'", key, ctx.getProperties().get(key)));
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

    @Override
    public HashSet<Node> getNodes() throws DatabaseException {
        String cypher = "match(n) where n:PC or n:OA or n:O or n:UA or n:U return n.id";
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

    @Override
    public HashSet<Long> getPolicies() throws DatabaseException {
        // get the cypher query
        String cypher = "match(pc:PC) return pc.id";
        // query neo4j for the nodes
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            HashSet<Long> policyIDs = new HashSet<>();
            while(rs.next()) {
                policyIDs.add(rs.getLong(1));
            }
            return policyIDs;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

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

    @Override
    public void assign(Node childCtx, Node parentCtx) throws DatabaseException, NullNodeCtxException, NullTypeException {
        //check that neither of the ctxs are null to avoid NPE
        if(childCtx == null || parentCtx == null) {
            throw new NullNodeCtxException();
        }

        String cypher = String.format("MATCH (a:%s{id: %d}), (b:%s{id: %d}) " +
                "CREATE (a)-[:assigned_to]->(b)", childCtx.getType(), childCtx.getID(), parentCtx.getType(), parentCtx.getID());
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

    @Override
    public void deassign(Node childCtx, Node parentCtx) throws DatabaseException, NullNodeCtxException, NullTypeException {
        //check that neither of the ctxs are null to avoid NPE
        if(childCtx == null || parentCtx == null) {
            throw new NullNodeCtxException();
        }

        String cypher = String.format("match (a:%s{id: %d})-[r:assigned_to]->(b:%s{id: %d}) delete r", childCtx.getType(), childCtx.getID(), parentCtx.getType(), parentCtx.getID());
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

    @Override
    public void associate(long uaID, long targetID, HashSet<String> operations) throws DatabaseException {
        String operationsStr = Neo4jHelper.setToCypherArray(operations);
        String cypher = String.format("MATCH (ua:UA{id: %d}), (target{id: %d}) " +
                "merge (ua)-[a:associated_with]->(target) set a.operations = %s", uaID, targetID, operationsStr);
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

    @Override
    public void dissociate(long uaID, long targetID) throws DatabaseException {
        String cypher = String.format("match (a{id:%d})-[r:associated_with]->(b{id:%d}) delete r", uaID, targetID);
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
