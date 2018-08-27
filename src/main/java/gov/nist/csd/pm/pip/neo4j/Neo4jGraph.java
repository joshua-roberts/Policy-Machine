package gov.nist.csd.pm.pip.neo4j;

import com.google.gson.Gson;
import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.csd.pm.pip.neo4j.Neo4jDatabase.*;
import static gov.nist.policyserver.common.Constants.ERR_NEO;

public class Neo4jGraph implements Graph {

    private Neo4jDatabase neo4j;

    public Neo4jGraph(String host, int port, String username, String password) throws DatabaseException {
        neo4j = new Neo4jDatabase(host, port, username, password);
    }

    @Override
    public Node createUser(String name, HashMap<String, String> properties) throws DatabaseException, NodeExistsException {
        return createNode(name, NodeType.USER, properties);
    }

    @Override
    public Node createUserAttribute(String name, HashMap<String, String> properties) throws DatabaseException, NodeExistsException {
        return createNode(name, NodeType.USER_ATTRIBUTE, properties);
    }

    @Override
    public Node createObject(String name, HashMap<String, String> properties) throws DatabaseException, NodeExistsException {
        return createNode(name, NodeType.OBJECT, properties);
    }

    @Override
    public Node createObjectAttribute(String name, HashMap<String, String> properties) throws DatabaseException, NodeExistsException {
        return createNode(name, NodeType.OBJECT_ATTRIBUTE, properties);
    }

    @Override
    public Node createPolicyClass(String name, HashMap<String, String> properties) throws DatabaseException, NodeExistsException {
        return createNode(name, NodeType.POLICY_CLASS, properties);
    }

    private Node createNode(String name, NodeType type, HashMap<String, String> properties) throws DatabaseException, NodeExistsException {
        Node node = new Node(name, type, properties);

        //check if node already exists
        try {
            getNode(node.getID());
            throw new NodeExistsException(node);
        }
        catch (NodeNotFoundException e) {
        }

        String cypher = String.format("create (n:NODE:%s{id: %d, name: '%s'})", type, node.getID(), name);
        String propStr = "";
        for (String key : properties.keySet()) {
            if (propStr.length() == 0) {
                propStr += String.format("%s: '%s'", key, properties.get(key));
            }
            else {
                propStr += String.format(", %s: '%s'", key, properties.get(key));
            }
        }
        cypher += String.format(" set n += {%s}", propStr);
        neo4j.execute(cypher);

        return node;
    }

    @Override
    public void deleteNode(long id) throws NodeNotFoundException, DatabaseException {
        Node node = getNode(id);

        String cypher = String.format("MATCH (n:%s) where n.id=%d DETACH DELETE n", node.getType(), id);
        neo4j.execute(cypher);
    }

    @Override
    public Node getNode(long id) throws NodeNotFoundException, DatabaseException {
        String cypher = String.format("match(n:NODE{id: %d}) return n", id);
        ResultSet rs = neo4j.execute(cypher);
        try {
            if (rs.next()) {
                return Neo4jDatabase.getNode(rs.getString(1));
            }

            throw new NodeNotFoundException(id);
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), "Error getting node from neo4j");
        }
    }

    @Override
    public void createAssignment(long childID, long parentID) throws NodeNotFoundException, AssignmentExistsException, InvalidAssignmentException, DatabaseException {
        Node child = getNode(childID);
        Node parent = getNode(parentID);

        if (isAssigned(child, parent)) {
            throw new AssignmentExistsException("Assignment exists between node " + childID + " and " + parentID);
        }

        Assignment.checkAssignment(child.getType(), parent.getType());

        String cypher = String.format("MATCH (a:%s {id:%d}), (b:%s {id:%d}) CREATE (a)-[:assigned_to]->(b)",
                child.getType(), child.getID(), parent.getType(), parent.getID());
        neo4j.execute(cypher);
    }

    private synchronized boolean isAssigned(Node child, Node parent) throws DatabaseException {
        String cypher = String.format("MATCH(n:%s {id: %d})-[r:assigned_to]->(m) return count(r:%s {id: %d})",
                child.getType(), child.getID(), parent.getType(), parent.getID());
        ResultSet rs = neo4j.execute(cypher);

        try {
            rs.next();
            return rs.getInt(1) != 0;
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), "Error checking if assignment exists in neo4j");
        }
    }

    @Override
    public void deleteAssignment(long childID, long parentID) throws NodeNotFoundException, AssignmentDoesNotExistException, DatabaseException {
        Node child = getNode(childID);
        Node parent = getNode(parentID);

        if (!isAssigned(child, parent)) {
            throw new AssignmentDoesNotExistException(childID, parentID);
        }

        String cypher = String.format("match (a:%s {id: %d})-[r:assigned_to]->(b:%s{id: %d}) delete r", child.getType(), childID, parent
                .getType(), parentID);
        neo4j.execute(cypher);
    }

    @Override
    public HashSet<Node> getChildren(long id) throws NodeNotFoundException, DatabaseException {
        Node node = getNode(id);

        String cypher = String.format("match(n)-[:assign_to]->(m:%s {id: %d}) return n", node.getType(), node.getID());
        ResultSet rs = neo4j.execute(cypher);
        return getNodesFromResultSet(rs);
    }

    @Override
    public HashSet<Node> getParents(long id) throws NodeNotFoundException, DatabaseException {
        Node node = getNode(id);

        String cypher = String.format("match(n)<-[:assign_to]-(m:%s {id: %d}) return n", node.getType(), node.getID());
        ResultSet rs = neo4j.execute(cypher);
        return getNodesFromResultSet(rs);
    }

    @Override
    public void createAssociation(long uaID, long targetID, String... operations) throws NodeNotFoundException, AssociationExistsException, InvalidAssociationException, DatabaseException {
        Node ua = getNode(uaID);
        Node target = getNode(targetID);

        if (isAssociated(ua, target)) {
            throw new AssociationExistsException(uaID, targetID);
        }
        
        Association.checkAssociation(ua.getType(), target.getType());

        String ops = toCypherArray(operations);
        String cypher = String.format("MATCH (ua:UA{id: %d}), (target:%s {id: %d}) " +
                "CREATE (ua)-[:association{operations: %s}]->(oa)",
                uaID, target.getType(), target.getID(), ops);
        neo4j.execute(cypher);
    }

    @Override
    public void updateAssociation(long uaID, long targetID, String... operations) throws NodeNotFoundException, AssociationDoesNotExistException, DatabaseException {
        Node ua = getNode(uaID);
        Node target = getNode(targetID);

        if (!isAssociated(ua, target)) {
            throw new AssociationDoesNotExistException(uaID, targetID);
        }

        String ops = toCypherArray(operations);
        String cypher = String.format("match(ua:UA {id: %d})-[a:association]->(target:%s {id: %d}) set a.operations: %s",
                ua.getID(), target.getType(), target.getID(), ops);
        neo4j.execute(cypher);
    }

    @Override
    public void deleteAssociation(long uaID, long targetID) throws NodeNotFoundException, AssociationDoesNotExistException, DatabaseException {
        Node ua = getNode(uaID);
        Node target = getNode(targetID);

        if (!isAssociated(ua, target)) {
            throw new AssociationDoesNotExistException(uaID, targetID);
        }

        String cypher = String.format("match(ua:UA {id: %d})-[a:association]->(target:%s {id: %d}) delete a",
                ua.getID(), target.getType(), target.getID());
    }
    
    private boolean isAssociated(Node ua, Node target) throws DatabaseException {
        String cypher = String.format("match(ua:UA {id: %d})-[a:association]->(target:%s {id: %d}) return count(n)", 
                ua.getID(), target.getType(), target.getID());
        ResultSet rs = neo4j.execute(cypher);
        try {
            rs.next();
            return rs.getInt(1) != 0;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    @Override
    public List<Association> getAssociations(long uaID) throws NodeNotFoundException, DatabaseException {
        Node ua = getNode(uaID);

        List<Association> associations = new ArrayList<>();

        String cypher = String.format("match(ua:UA {id: %d})-[a:association]->(target) return ua,oa,a.operations",
                ua.getID());
        ResultSet rs = neo4j.execute(cypher);
        try {
            while (rs.next()) {
                Node startNode = Neo4jDatabase.getNode(rs.getString(1));
                Node endNode = Neo4jDatabase.getNode(rs.getString(2));
                HashSet<String> ops = getStringSetFromJson(rs.getString(3));
                Association assoc = new Association(startNode, endNode, ops);
                associations.add(assoc);
            }
            return associations;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }
}
