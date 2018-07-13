package gov.nist.policyserver.dao.neo4j;

import gov.nist.policyserver.analytics.PmAnalytics;
import gov.nist.policyserver.dao.DAOManager;
import gov.nist.policyserver.dao.GraphDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.InvalidPropertyException;
import gov.nist.policyserver.graph.PmGraph;
import gov.nist.policyserver.helpers.JsonHelper;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.Property;
import gov.nist.policyserver.model.graph.relationships.Assignment;
import gov.nist.policyserver.model.graph.relationships.Association;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static gov.nist.policyserver.common.Constants.ERR_NEO;
import static gov.nist.policyserver.dao.neo4j.Neo4jHelper.*;

public class Neo4jGraphDAO implements GraphDAO {

    private PmGraph    graph;
    private PmAnalytics analytics;
    private Connection connection;

    public Neo4jGraphDAO(Connection connection) throws DatabaseException, InvalidPropertyException {
        this.connection = connection;
        buildGraph();
        analytics = new PmAnalytics();
    }

    @Override
    public PmAnalytics getAnalytics() {
        return analytics;
    }

    @Override
    public PmGraph getGraph() {
        return graph;
    }

    @Override
    public PmGraph buildGraph() throws DatabaseException, InvalidPropertyException {
        System.out.println("Building graph...");

        graph = new PmGraph();

        System.out.print("Getting nodes...");
        List<Node> nodes = getNodes();
        for(Node node : nodes){
            graph.addNode(node);
        }
        System.out.println("DONE");


        System.out.print("Getting assignments...");
        List<Assignment> assignments = getAssignments();
        for(Assignment assignment : assignments){
            graph.createAssignment(graph.getNode(assignment.getChild().getId()), graph.getNode(assignment.getParent().getId()));
        }
        System.out.println("DONE");

        System.out.print("Getting associations...");
        List<Association> associations = getAssociations();
        for(Association assoc : associations){
            graph.createAssociation(assoc.getChild(), assoc.getParent(), assoc.getOps(), assoc.isInherit());
        }
        System.out.println("DONE");

        return graph;
    }

    @Override
    public List<Node> getNodes() throws DatabaseException, InvalidPropertyException {
        String cypher = "match(n) where n:PC or n:OA or n:O or n:UA or n:U return n";
        ResultSet rs = execute(connection, cypher);
        List<Node> nodes = getNodesFromResultSet(rs);
        for(Node node : nodes){
            node.setProperties(getNodeProps(node));
        }

        return nodes;
    }

    private List<Property> getNodeProps(Node node) throws DatabaseException, InvalidPropertyException {
        String cypher = "match(n:" + node.getType() + "{id:" + node.getId() + "}) return n";
        ResultSet rs = execute(connection, cypher);
        try {
            List<Property> props = new ArrayList<>();
            while(rs.next()){
                String json = rs.getString(1);
                props.addAll(JsonHelper.getPropertiesFromJson(json));
            }
            return props;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    @Override
    public List<Assignment> getAssignments() throws DatabaseException {
        List<Assignment> assignments = new ArrayList<>();

        String cypher = "match(n)-[r:assigned_to]->(m) return n, r, m";
        ResultSet rs = execute(connection, cypher);
        try {
            while (rs.next()) {
                Node startNode = JsonHelper.getNodeFromJson(rs.getString(1));
                Node endNode = JsonHelper.getNodeFromJson(rs.getString(3));
                assignments.add(new Assignment(startNode, endNode));
            }
            return assignments;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    @Override
    public List<Association> getAssociations() throws DatabaseException {
        List<Association> associations = new ArrayList<>();

        String cypher = "match(ua:UA)-[a:association]->(oa:OA) return ua,oa,a.operations,a.inherit;";
        ResultSet rs = execute(connection, cypher);
        try {
            while (rs.next()) {
                Node startNode = JsonHelper.getNodeFromJson(rs.getString(1));
                Node endNode = JsonHelper.getNodeFromJson(rs.getString(2));
                HashSet<String> ops = JsonHelper.getStringSetFromJson(rs.getString(3));
                boolean inherit = Boolean.valueOf(rs.getString(4));
                Association assoc = new Association(startNode, endNode, ops, inherit);
                associations.add(assoc);
            }
            return associations;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    @Override
    public void reset() throws DatabaseException {
        String cypher = "match(n) where n.id > 0 detach delete n";
        execute(connection, cypher);
    }
}
