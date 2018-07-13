package gov.nist.policyserver.dao.sql;

import gov.nist.policyserver.analytics.PmAnalytics;
import gov.nist.policyserver.dao.DAOManager;
import gov.nist.policyserver.dao.GraphDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.InvalidNodeTypeException;
import gov.nist.policyserver.exceptions.InvalidPropertyException;
import gov.nist.policyserver.graph.PmGraph;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.nodes.Property;
import gov.nist.policyserver.model.graph.relationships.Assignment;
import gov.nist.policyserver.model.graph.relationships.Association;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class SqlGraphDAO implements GraphDAO {

    private PmGraph    graph;
    private PmAnalytics analytics;
    private Connection conn;

    public SqlGraphDAO(Connection connection) {
        this.conn = connection;
        buildGraph();
        analytics = new PmAnalytics();
    }

    public PmGraph getGraph() {
        return graph;
    }

    @Override
    public PmAnalytics getAnalytics() {
        return analytics;
    }

    @Override
    public PmGraph buildGraph() {
        try {
            graph = new PmGraph();

            List<Node> nodes = getNodes();
            for (Node node : nodes) {
                if (!node.getType().equals(NodeType.OS)) {
                    graph.addNode(node);
                }
            }

            List<Assignment> assignments = getAssignments();
            for (Assignment assignment : assignments) {
                Node start = assignment.getChild();
                Node end = assignment.getParent();
                if (graph.getNode(start.getId()) == null || graph.getNode(end.getId()) == null) {
                    continue;
                }
                graph.createAssignment(assignment.getChild(), assignment.getParent());
            }

            List<Association> associations = null;
            associations = getAssociations();
            for (Association assoc : associations) {
                graph.createAssociation(assoc.getChild(), assoc.getParent(), assoc.getOps(), assoc.isInherit());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return graph;
    }

    @Override
    public List<Node> getNodes() throws DatabaseException, InvalidPropertyException, InvalidNodeTypeException {
        try {
            List<Node> nodes = new ArrayList<>();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select node_id,name,node_type_id,description from node");
            while (rs.next()) {
                long id = rs.getInt(1);
                String name = rs.getString(2);
                NodeType type = NodeType.toNodeType(rs.getInt(3));
                String description = rs.getString(4);
                Node node = new Node(id, name, type, description);

                Statement propStmt = conn.createStatement();
                List<Property> props = getNodeProps(node);
                for(int i=0; i < props.size();i++){
                    node.addProperty(props.get(i));
                }

                nodes.add(node);
            }
            return nodes;
        }catch(SQLException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    private List<Property> getNodeProps(Node node) throws DatabaseException, InvalidPropertyException {
        try {
            List<Property> props = new ArrayList<>();
            Statement stmt = conn.createStatement();
            ResultSet propRs = stmt.executeQuery("SELECT property_key, NODE_PROPERTY.property_value FROM NODE_PROPERTY WHERE PROPERTY_NODE_ID = " + node.getId());
            while(propRs.next()){
                String key = propRs.getString(1);
                String value = propRs.getString(2);
                Property prop = new Property(key, value);
                props.add(prop);
            }
            return props;

        } catch(SQLException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<Assignment> getAssignments() throws DatabaseException, InvalidNodeTypeException {
        try{
            List<Assignment> relationships = new ArrayList<>();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT start_node_id,a.name,a.node_type_id,end_node_id,b.name,b.node_type_id FROM assignment join node a on start_node_id = a.node_id join node b on end_node_id=b.node_id where assignment.depth=1;");
            while(rs.next()){
                long id = rs.getInt(1);
                String name = rs.getString(2);
                NodeType type = NodeType.toNodeType(rs.getInt(3));
                Node endNode = new Node(id, name, type);
                if(type.equals(NodeType.OS))continue;

                id = rs.getInt(4);
                name = rs.getString(5);
                type = NodeType.toNodeType(rs.getInt(6));
                Node startNode = new Node(id, name, type);
                if(type.equals(NodeType.OS))continue;

                relationships.add(new Assignment(startNode, endNode));
            }
            return relationships;
        } catch(SQLException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<Association> getAssociations() throws DatabaseException, InvalidNodeTypeException {
        try{
            List<Association> associations = new ArrayList<>();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ua_id,a.name,a.node_type_id, get_operations(opset_id),oa_id,b.name,b.node_type_id FROM association join node a on ua_id = a.node_id join node b on oa_id=b.node_id");
            while (rs.next()) {
                long id = rs.getInt(1);
                String name = rs.getString(2);
                NodeType type = NodeType.toNodeType(rs.getInt(3));
                Node startNode = new Node(id, name, type);

                HashSet<String> ops = new HashSet<>(Arrays.asList(rs.getString(4).split(",")));

                id = rs.getInt(5);
                name = rs.getString(6);
                type = NodeType.toNodeType(rs.getInt(7));
                Node endNode = new Node(id, name, type);

                associations.add(new Association(startNode, endNode, ops, true));
            }
            return associations;
        } catch(SQLException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public void reset() throws DatabaseException {

    }
}
