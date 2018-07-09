package gov.nist.policyserver.dao.neo4j;

import gov.nist.policyserver.analytics.PmAnalytics;
import gov.nist.policyserver.dao.DAOManager;
import gov.nist.policyserver.dao.GraphDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
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
    private DAOManager daoManager = DAOManager.instance;

    public Neo4jGraphDAO(Connection connection) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        this.connection = connection;
        System.out.println("in constructor");
        buildGraph();
        analytics = new PmAnalytics();
    }

    public PmAnalytics getAnalytics() {
        return analytics;
    }

    @Override
    public PmGraph getGraph() {
        return graph;
    }

    @Override
    public PmGraph buildGraph() throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        System.out.println("Building graph...");

        graph = new PmGraph();

        System.out.print("Getting nodes...");
        List<Node> nodes = null;
        nodes = daoManager.getNodesDAO().getNodes();
        for(Node node : nodes){
            graph.addNode(node);
        }
        System.out.println("DONE");


        System.out.print("Getting assignments...");
        List<Assignment> assignments = daoManager.getAssignmentsDAO().getAssignments();
        for(Assignment assignment : assignments){
            System.out.println(assignment.getChild() + "-->" + assignment.getParent());
            graph.createAssignment(graph.getNode(assignment.getChild().getId()), graph.getNode(assignment.getParent().getId()));
        }
        System.out.println("DONE");

        System.out.print("Getting associations...");
        List<Association> associations = daoManager.getAssociationsDAO().getAssociations();
        for(Association assoc : associations){
            graph.createAssociation(assoc.getChild(), assoc.getParent(), assoc.getOps(), assoc.isInherit());
        }
        System.out.println("DONE");

        return graph;
    }

    @Override
    public void reset() throws DatabaseException {
        String cypher = "match(n) detach delete n";
        execute(connection, cypher);
    }
}
