package gov.nist.policyserver.dao.sql;

import gov.nist.policyserver.analytics.PmAnalytics;
import gov.nist.policyserver.dao.DAOManager;
import gov.nist.policyserver.dao.GraphDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.graph.PmGraph;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.relationships.Assignment;
import gov.nist.policyserver.model.graph.relationships.Association;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SqlGraphDAO implements GraphDAO {

    private PmGraph    graph;
    private PmAnalytics analytics;
    private Connection conn;
    private DAOManager daoManager = null;

    public SqlGraphDAO(Connection connection) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        this.conn = connection;
        buildGraph();
        analytics = new PmAnalytics();
    }

    public PmGraph getGraph() {
        return graph;
    }

    @Override
    public PmAnalytics getAnalytics() {
        return null;
    }

    @Override
    public PmGraph buildGraph() throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        try {
            graph = new PmGraph();
            List<Node> nodes = null;
            daoManager = DAOManager.getDaoManager();
            nodes = daoManager.getNodesDAO().getNodes();

            for (Node node : nodes) {
                if (!node.getType().equals(NodeType.OS)) {
                    graph.addNode(node);
                }
            }

            List<Assignment> assignments = null;
            assignments = daoManager.getAssignmentsDAO().getAssignments();

            for (Assignment assignment : assignments) {
                Node start = assignment.getChild();
                Node end = assignment.getParent();
                if (graph.getNode(start.getId()) == null || graph.getNode(end.getId()) == null) {
                    continue;
                }
                graph.createAssignment(assignment.getChild(), assignment.getParent());
            }

            List<Association> associations = null;
            associations = daoManager.getAssociationsDAO().getAssociations();
            for (Association assoc : associations) {
                graph.createAssociation(assoc.getChild(), assoc.getParent(), assoc.getOps(), assoc.isInherit());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return graph;
    }
    @Override
    public void reset() throws DatabaseException {

    }
}
