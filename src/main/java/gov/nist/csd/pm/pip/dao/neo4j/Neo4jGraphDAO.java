package gov.nist.csd.pm.pip.dao.neo4j;

import com.google.gson.Gson;
import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidPropertyException;
import gov.nist.csd.pm.model.graph.Assignment;
import gov.nist.csd.pm.model.graph.Association;
import gov.nist.csd.pm.pdp.analytics.PmAnalytics;
import gov.nist.csd.pm.pip.dao.GraphDAO;
import gov.nist.csd.pm.pip.graph.PmGraph;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.prohibitions.Prohibition;
import gov.nist.csd.pm.model.prohibitions.ProhibitionResource;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.csd.pm.pep.response.ApiResponseCodes.ERR_NEO;

public class Neo4jGraphDAO implements GraphDAO {

    private PmGraph    graph;
    private PmAnalytics analytics = new PmAnalytics();

    private Neo4jConnection neo4j;

    public Neo4jGraphDAO(DatabaseContext ctx) throws DatabaseException, InvalidPropertyException {
        neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
        buildGraph();
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
            System.out.println(assignment.getChild() + "-->" + assignment.getParent());
            graph.createAssignment(graph.getNode(assignment.getChild().getID()), graph.getNode(assignment.getParent().getID()));
        }
        System.out.println("DONE");

        System.out.print("Getting associations...");
        List<Association> associations = getAssociations();
        for(Association assoc : associations){
            graph.createAssociation(assoc.getChild(), assoc.getParent(), assoc.getOps());
        }
        System.out.println("DONE");

        System.out.print("Getting prohibitions...");
        List<Prohibition> prohibitions = getProhibitions();
        for(Prohibition prohibition : prohibitions) {
            analytics.addProhibition(prohibition);
        }
        System.out.println("DONE");
        return graph;
    }

    @Override
    public List<Node> getNodes() throws DatabaseException, InvalidPropertyException {
        String cypher = "match(n) where n:PC or n:OA or n:O or n:UA or n:U return n";
        ResultSet rs = neo4j.execute(cypher);
        List<Node> nodes = neo4j.getNodesFromResultSet(rs);
        for(Node node : nodes){
            node.setProperties(getNodeProps(node));
        }

        return nodes;
    }

    private Map<String, String> getNodeProps(Node node) throws DatabaseException, InvalidPropertyException {
        String cypher = "match(n:" + node.getType() + "{id:" + node.getID() + "}) return n";
        ResultSet rs = neo4j.execute(cypher);
        try {
            Map<String, String> props = new HashMap<>();
            while(rs.next()){
                String json = rs.getString(1);
                props.putAll(neo4j.getPropertiesFromJson(json));
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
        ResultSet rs = neo4j.execute(cypher);
        try {
            while (rs.next()) {
                Node startNode = neo4j.getNodeFromJson(rs.getString(1));
                Node endNode = neo4j.getNodeFromJson(rs.getString(3));
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

        String cypher = "match(ua:UA)-[a:association]->(oa:OA) return ua,oa,a.operations";
        ResultSet rs = neo4j.execute(cypher);
        try {
            while (rs.next()) {
                Node startNode = neo4j.getNodeFromJson(rs.getString(1));
                Node endNode = neo4j.getNodeFromJson(rs.getString(2));
                HashSet<String> ops = neo4j.getStringSetFromJson(rs.getString(3));
                Association assoc = new Association(startNode, endNode, ops);
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
        String cypher = "match(n) detach delete n";
        neo4j.execute(cypher);
    }

    @Override
    public List<Prohibition> getProhibitions() throws DatabaseException {
        List<Prohibition> prohibitions = new ArrayList<>();

        String cypher = "match(p:prohibition) return p";
        ResultSet rs = neo4j.execute(cypher);
        try {
            while (rs.next()) {
                String json = rs.getString(1);
                Prohibition prohibition = neo4j.getProhibition(json);

                //get subject
                cypher = "match(d:prohibition{name:'" + prohibition.getName() + "'})<-[:prohibition]-(s) return s";
                ResultSet rs2 = neo4j.execute(cypher);
                if(rs2.next()) {
                    json = rs2.getString(1);
                    prohibition.setSubject(neo4j.getProhibitionSubject(json));
                }

                //get resources
                cypher = "match(d:prohibition{name:'" + prohibition.getName() + "'})-[r:prohibition]->(s) return s, r.complement";
                rs2 = neo4j.execute(cypher);
                while(rs2.next()) {
                    json = rs2.getString(1);
                    Node node = new Gson().fromJson(json, Node.class);

                    boolean complement = rs2.getBoolean(2);
                    prohibition.addResource(new ProhibitionResource(node.getID(), complement));
                }

                prohibitions.add(prohibition);
            }
        }catch(SQLException e){
            throw new DatabaseException(ERR_NEO, "Error getting prohibitions from nodes");
        }

        return prohibitions;
    }
}
