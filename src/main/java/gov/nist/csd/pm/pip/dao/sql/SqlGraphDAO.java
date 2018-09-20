package gov.nist.csd.pm.pip.dao.sql;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidNodeTypeException;
import gov.nist.csd.pm.model.exceptions.InvalidProhibitionSubjectTypeException;
import gov.nist.csd.pm.model.graph.Assignment;
import gov.nist.csd.pm.model.graph.Association;
import gov.nist.csd.pm.model.graph.NodeType;
import gov.nist.csd.pm.model.prohibitions.ProhibitionResource;
import gov.nist.csd.pm.model.prohibitions.ProhibitionSubject;
import gov.nist.csd.pm.model.prohibitions.ProhibitionSubjectType;
import gov.nist.csd.pm.pdp.analytics.PmAnalytics;
import gov.nist.csd.pm.pip.dao.GraphDAO;
import gov.nist.csd.pm.pip.graph.PmGraph;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.prohibitions.Prohibition;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class SqlGraphDAO implements GraphDAO {

    private PmGraph         graph;
    private PmAnalytics     analytics = new PmAnalytics();
    private MySQLConnection mysql;

    public SqlGraphDAO(DatabaseContext ctx) throws DatabaseException {
        mysql = new MySQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
        buildGraph();
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
                if (graph.getNode(start.getID()) == null || graph.getNode(end.getID()) == null) {
                    continue;
                }
                graph.createAssignment(assignment.getChild(), assignment.getParent());
            }

            List<Association> associations = null;
            associations = getAssociations();
            for (Association assoc : associations) {
                graph.createAssociation(assoc.getChild(), assoc.getParent(), assoc.getOps());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return graph;
    }

    @Override
    public List<Node> getNodes() throws DatabaseException, InvalidNodeTypeException {
        try {
            List<Node> nodes = new ArrayList<>();
            Statement stmt = mysql.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("select node_id,name,node_type_id from node");
            while (rs.next()) {
                long id = rs.getInt(1);
                nodes.add(getNode(id));
            }
            return nodes;
        }catch(SQLException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    private Node getNode(long id) throws SQLException, InvalidNodeTypeException, DatabaseException {
        Statement stmt = mysql.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("select node_id,name,node_type_id from node where node_id="+id);
        rs.next();
        String name = rs.getString(2);
        NodeType type = NodeType.toNodeType(rs.getInt(3));
        HashMap<String, String> properties = getNodeProps(id);

        return new Node(id, name, type, properties);
    }

    private HashMap<String, String> getNodeProps(long nodeID) throws DatabaseException {
        try {
            HashMap<String, String> props = new HashMap<>();
            Statement stmt = mysql.getConnection().createStatement();
            ResultSet propRs = stmt.executeQuery("SELECT property_key, NODE_PROPERTY.property_value FROM NODE_PROPERTY WHERE PROPERTY_NODE_ID = " + nodeID);
            while(propRs.next()){
                String key = propRs.getString(1);
                String value = propRs.getString(2);
                props.put(key, value);
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
            Statement stmt = mysql.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT start_node_id,end_node_id FROM assignment join node a on start_node_id = a.node_id join node b on end_node_id=b.node_id where assignment.depth=1;");
            while(rs.next()){
                long id = rs.getLong(1);
                Node endNode = getNode(id);
                if(endNode.getType().equals(NodeType.OS))continue;

                id = rs.getLong(4);
                Node startNode = getNode(id);
                if(endNode.getType().equals(NodeType.OS))continue;

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
            Statement stmt = mysql.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ua_id,a.name,a.node_type_id, get_operations(opset_id),oa_id,b.name,b.node_type_id FROM association join node a on ua_id = a.node_id join node b on oa_id=b.node_id");
            while (rs.next()) {
                long id = rs.getLong(1);
                Node startNode = getNode(id);

                HashSet<String> ops = new HashSet<>(Arrays.asList(rs.getString(4).split(",")));

                id = rs.getInt(5);
                Node endNode = getNode(id);

                associations.add(new Association(startNode, endNode, ops));
            }
            return associations;
        } catch(SQLException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public void reset() throws DatabaseException {

    }

    @Override
    public List<Prohibition> getProhibitions() throws DatabaseException {
        List<Prohibition> prohibitions = new ArrayList<>();
        try {
            Statement stmt = mysql.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT deny_name, abbreviation, user_attribute_id, is_intersection, " +
                    "object_attribute_id, object_complement, " +
                    "get_operation_name(deny_operation_id)  " +
                    "FROM deny, deny_obj_attribute, deny_operation, deny_type " +
                    "WHERE deny.deny_id = deny_obj_attribute.deny_id " +
                    "AND deny.deny_id = deny_operation.deny_id " +
                    "AND deny_type.deny_type_id = deny.deny_type_id");

            while(rs.next()) {
                //prohibitions and subject information
                String deny_name = rs.getString(1);
                String type_abbr = rs.getString(2);
                int ua_id = rs.getInt(3);
                boolean intersection = rs.getInt(4) == 1;
                //resource information
                int object_attribute_id = rs.getInt(5);
                boolean object_complement = rs.getInt(6) == 1;
                //operation information
                String operation_name = rs.getString(7);

                Prohibition p = analytics.getProhibition(deny_name);
                if (p == null) {
                    ProhibitionSubject subject = new ProhibitionSubject(ua_id, ProhibitionSubjectType.toProhibitionSubjectType(type_abbr));
                    List<ProhibitionResource> resources = new ArrayList<>();
                    resources.add(new ProhibitionResource(object_attribute_id, object_complement));
                    HashSet<String> operations = new HashSet<>();
                    operations.add(operation_name);

                    p = new Prohibition(subject, resources, deny_name, operations, intersection);
                    analytics.addProhibition(p);
                } else {
                    boolean found = false;
                    List<ProhibitionResource> prs = p.getResources();
                    for (ProhibitionResource pr: prs) {
                        if (pr.getResourceID() == object_attribute_id) {
                            found = true;
                        }
                    }
                    if (!found) {
                        p.addResource(new ProhibitionResource(object_attribute_id, object_complement));
                    }
                    HashSet<String> ops = p.getOperations();
                    ops.add(operation_name);
                    p.setOperations(ops);
                }
            }
        } catch(SQLException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        } catch (InvalidProhibitionSubjectTypeException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
        return prohibitions;
    }
}
