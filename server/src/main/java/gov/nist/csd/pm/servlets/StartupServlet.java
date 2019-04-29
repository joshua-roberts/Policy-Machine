package gov.nist.csd.pm.servlets;

import gov.nist.csd.pm.epp.EPP;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.MemGraph;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.graph.model.relationships.Assignment;
import gov.nist.csd.pm.graph.model.relationships.Association;
import gov.nist.csd.pm.pap.*;
import gov.nist.csd.pm.pap.obligations.ObligationsPAP;
import gov.nist.csd.pm.pdp.PDP;
import gov.nist.csd.pm.pip.db.DatabaseContext;
import gov.nist.csd.pm.pip.graph.Neo4jGraph;
import gov.nist.csd.pm.pip.loader.graph.GraphLoader;
import gov.nist.csd.pm.pip.loader.graph.Neo4jGraphLoader;
import gov.nist.csd.pm.pip.obligations.MemObligations;
import gov.nist.csd.pm.pip.obligations.Neo4jObligations;
import gov.nist.csd.pm.pip.prohibitions.Neo4jProhibitionsDAO;
import gov.nist.csd.pm.prohibitions.MemProhibitionsDAO;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

@WebListener
public class StartupServlet implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // init pap
        try {
            initPAP();
        }
        catch (PMException e) {
            e.printStackTrace();
        }

        // init epp
        EPP.initPAP(PAP.getPAP());

        // init pdp
        initPDP();

        // add the pdp to the epp
        EPP.initPDP(PDP.getPDP());

    }

    private void initPDP() {
        PDP.init(EPP.getEPP(), PAP.getPAP());
    }

    private void initPAP() throws PMException {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("db.config");
            if (is == null) {
                throw new PMException("/resources/db.config does not exist");
            }

            Properties props = new Properties();
            props.load(is);

            DatabaseContext dbCtx = new DatabaseContext(
                    props.getProperty("host"),
                    Integer.valueOf(props.getProperty("port")),
                    props.getProperty("username"),
                    props.getProperty("password"),
                    props.getProperty("schema")
            );

            // instantiate the neo4j graph and check that the super config is in the graph.
            Neo4jGraph neo4jGraph = new Neo4jGraph(dbCtx);
            SuperGraph.check(neo4jGraph);

            // load the graph into memory
            GraphLoader loader = new Neo4jGraphLoader(dbCtx);
            MemGraph memGraph = new MemGraph();

            Set<Node> nodes = loader.getNodes();
            for(Node node : nodes) {
                memGraph.createNode(node.getID(), node.getName(), node.getType(), node.getProperties());
            }

            Set<Assignment> assignments = loader.getAssignments();
            for(Assignment assignment : assignments) {
                long childID = assignment.getSourceID();
                long parentID = assignment.getTargetID();
                memGraph.assign(childID, parentID);
            }

            Set<Association> associations = loader.getAssociations();
            for(Association association : associations) {
                long uaID = association.getSourceID();
                long targetID = association.getTargetID();
                Set<String> operations = association.getOperations();
                memGraph.associate(uaID, targetID, operations);
            }

            PAP.init(
                    new GraphPAP(memGraph, neo4jGraph),
                    new ProhibitionsPAP(new MemProhibitionsDAO(), new Neo4jProhibitionsDAO(dbCtx)),
                    new ObligationsPAP(new MemObligations(), new Neo4jObligations())
            );
        }
        catch (IOException e) {
            throw new PMException(e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
