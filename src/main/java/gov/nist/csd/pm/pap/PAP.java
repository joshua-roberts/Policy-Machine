package gov.nist.csd.pm.pap;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.util.NodeUtils;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.Graph;
import gov.nist.csd.pm.graph.MemGraph;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.graph.model.nodes.NodeType;
import gov.nist.csd.pm.graph.model.relationships.Assignment;
import gov.nist.csd.pm.graph.model.relationships.Association;
import gov.nist.csd.pm.pip.db.DatabaseContext;
import gov.nist.csd.pm.pap.sessions.SessionManager;
import gov.nist.csd.pm.pip.graph.Neo4jGraph;
import gov.nist.csd.pm.pip.loader.graph.GraphLoader;
import gov.nist.csd.pm.pip.loader.graph.Neo4jGraphLoader;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static gov.nist.csd.pm.common.constants.Operations.ALL_OPERATIONS;
import static gov.nist.csd.pm.common.constants.Properties.*;
import static gov.nist.csd.pm.common.util.NodeUtils.generatePasswordHash;
import static gov.nist.csd.pm.graph.model.nodes.NodeType.UA;

/**
 * PAP is the Policy Information Point. The purpose of the PAP is to expose the underlying policy data to the PDP and EPP.
 * It initializes the backend using the connection properties in /resource/db.config.  This servlet can
 * be access via ../index.jsp upon starting the server.The PAP also stores the in memory graph that will be used for
 * decision making.
 */
public class PAP {

    /**
     * A static instance of the PAP to be used by the PDP.
     */
    private static PAP PAP;

    private GraphPAP        graphPAP;
    private ProhibitionsPAP prohibitionsPAP;
    private SessionManager  sessionManager;

    private Node superPC;
    private Node superPCRep;
    private Node superUA1;
    private Node superUA2;
    private Node superU;
    private Node superOA;
    private Node superO;

    /**
     * Initialize a the static PAP variable if not already initialized.
     * @return a PAP instance.
     * @throws PMGraphException if there is an error checking the metadata in the graph.
     * @throws PMDBException if there is an error connecting to the database.
     * @throws PMConfigurationException if there is an error with the configuration of the PAP.
     * @throws PMAuthorizationException if the current user cannot carryout an action.
     */
    public static synchronized PAP getPAP() throws PMException {
        if(PAP == null) {
            PAP = new PAP();
        }
        return PAP;
    }

    /**
     * Reinitialize the PAP with the given database context information.
     *
     * @param ctx The database context to reinitialize the PAP with.
     * @return a PAP instance
     * @throws PMGraphException if there is an error checking the metadata in the graph.
     * @throws PMDBException if there is an error connecting to the database.
     * @throws PMConfigurationException if there is an error with the configuration of the PAP.
     * @throws PMAuthorizationException if the current user cannot carryout an action.
     */
    public static synchronized void getPAP(DatabaseContext ctx) throws PMException {
        PAP = new PAP(ctx);
    }

    private PAP() throws PMException {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("db.config");
            if (is == null) {
                throw new PMConfigurationException("/resource/db.config does not exist");
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
            init(dbCtx);
        }
        catch (IOException e) {
            throw new PMConfigurationException(e.getMessage());
        }
    }

    private PAP(DatabaseContext ctx) throws PMException {
        init(ctx);
    }

    /**
     * Instantiate the backend database, in memory graph, and DAOs. Then, make sure the super user, user attribute,
     * and policy class exist, if not, add them to the graph.  This is done because there must always be a super or root
     * user.
     *
     * @param ctx the database connection information.
     */
    private void init(DatabaseContext ctx) throws PMException {
        // load the graph from the database into memory
        GraphLoader loader = new Neo4jGraphLoader(ctx);
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

        // create a new graph pap with the in memory graph and db graph
        graphPAP = new GraphPAP(memGraph, new Neo4jGraph(ctx));
        prohibitionsPAP = new ProhibitionsPAP(ctx);
        sessionManager = new SessionManager();

        // check that the super nodes are created
        loadSuper();
    }

    /**
     * Check that all of the super meta data is in the graph.  Create any nodes, assignments, or associations that do not
     * exist but should
     */
    public void loadSuper() throws PMException {
        Random rand = new Random();
        HashMap<String, String> props = NodeUtils.toProperties(NAMESPACE_PROPERTY, "super");

        Set<Node> nodes = getGraphPAP().search("super_ua1", UA.toString(), props);
        if(nodes.isEmpty()) {
            superUA1 = getGraphPAP().createNode(rand.nextLong(), "super_ua1", UA, props);
        } else {
            superUA1 = nodes.iterator().next();
        }
        nodes = getGraphPAP().search("super_ua2", UA.toString(), props);
        if(nodes.isEmpty()) {
            superUA2 = getGraphPAP().createNode(rand.nextLong(), "super_ua2", UA, props);
        } else {
            superUA2 = nodes.iterator().next();
        }
        nodes = getGraphPAP().search("super", NodeType.U.toString(), props);
        if(nodes.isEmpty()) {
            try {
                props.put(PASSWORD_PROPERTY, generatePasswordHash("super"));
            }
            catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new PMGraphException(e.getMessage());
            }
            superU = getGraphPAP().createNode(rand.nextLong(), "super", NodeType.U, props);
            props.remove(PASSWORD_PROPERTY);
        } else {
            superU = nodes.iterator().next();
        }

        nodes = getGraphPAP().search("super", NodeType.OA.toString(), props);
        if(nodes.isEmpty()) {
            superOA = getGraphPAP().createNode(rand.nextLong(), "super", NodeType.OA, props);
        } else {
            superOA = nodes.iterator().next();
        }
        nodes = getGraphPAP().search("super", NodeType.O.toString(), props);
        if(nodes.isEmpty()) {
            superO = getGraphPAP().createNode(rand.nextLong(), "super", NodeType.O, props);
        } else {
            superO = nodes.iterator().next();
        }
        nodes = getGraphPAP().search("super rep", NodeType.OA.toString(), props);
        if(nodes.isEmpty()) {
            superPCRep = getGraphPAP().createNode(rand.nextLong(), "super rep", NodeType.OA, props);
        } else {
            superPCRep = nodes.iterator().next();
        }
        nodes = getGraphPAP().search("super", NodeType.PC.toString(), props);
        if(nodes.isEmpty()) {
            // add the rep oa ID to the properties
            props.put(REP_PROPERTY, String.valueOf(superPCRep.getID()));
            superPC = getGraphPAP().createNode(rand.nextLong(), "super", NodeType.PC, props);
            props.remove(REP_PROPERTY);
        } else {
            superPC = nodes.iterator().next();

            // make sure the rep ID property is correct
            // if the rep ID for the pc node is null, empty, or doesn't match the current rep ID, update the ID
            String repID = superPC.getProperties().get(REP_PROPERTY);
            if(repID == null || repID.isEmpty() || !repID.equals(String.valueOf(superPCRep.getID()))) {
                repID = String.valueOf(superPCRep.getID());
                superPC.getProperties().put(REP_PROPERTY, repID);
                // update the node
                getGraphPAP().updateNode(superPC.getID(), null, superPC.getProperties());
            }
        }

        // check super ua1 is assigned to super pc
        Set<Long> children = getGraphPAP().getChildren(superPC.getID());
        if(!children.contains(superUA1.getID())) {
            getGraphPAP().assign(superUA1.getID(), superPC.getID());
        }
        // check super ua2 is assigned to super pc
        children = getGraphPAP().getChildren(superPC.getID());
        if(!children.contains(superUA2.getID())) {
            getGraphPAP().assign(superUA2.getID(), superPC.getID());
        }
        // check super user is assigned to super ua1
        children = getGraphPAP().getChildren(superUA1.getID());
        if(!children.contains(superU.getID())) {
            getGraphPAP().assign(superU.getID(), superUA1.getID());
        }
        // check super user is assigned to super ua2
        children = getGraphPAP().getChildren(superUA2.getID());
        if(!children.contains(superU.getID())) {
            getGraphPAP().assign(superU.getID(),superUA2.getID());
        }
        // check super oa is assigned to super pc
        children = getGraphPAP().getChildren(superPC.getID());
        if(!children.contains(superOA.getID())) {
            getGraphPAP().assign(superOA.getID(), superPC.getID());
        }
        // check that the super rep is assigned to super oa
        children = getGraphPAP().getChildren(superOA.getID());
        if(!children.contains(superPCRep.getID())) {
            getGraphPAP().assign(superPCRep.getID(), superOA.getID());
        }
        // check super o is assigned to super oa
        if(!children.contains(superO.getID())) {
            getGraphPAP().assign(superO.getID(), superOA.getID());
        }

        // associate super ua to super oa
        getGraphPAP().associate(superUA2.getID(), superOA.getID(), new HashSet<>(Arrays.asList(ALL_OPERATIONS)));
        getGraphPAP().associate(superUA1.getID(), superUA2.getID(), new HashSet<>(Arrays.asList(ALL_OPERATIONS)));
    }

    public GraphPAP getGraphPAP() {
        return graphPAP;
    }

    public ProhibitionsPAP getProhibitionsPAP() {
        return prohibitionsPAP;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public Node getSuperPC() {
        return superPC;
    }

    public Node getSuperUA1() {
        return superUA1;
    }

    public Node getSuperUA2() {
        return superUA2;
    }

    public Node getSuperU() {
        return superU;
    }

    public Node getSuperOA() {
        return superOA;
    }

    public Node getSuperO() {
        return superO;
    }

    public void reset() throws PMException {
        // delete nodes
        graphPAP.reset();

        // delete prohibitions
        prohibitionsPAP.reset();

        // delete sessions
        sessionManager.reset();
    }

    /**
     * Reinitialize the PAP.  Set up the NGAC backend.  This is primarily used when loading a configuration.  Loading
     * a configuration makes changes to the database but not the in memory graph.  So, the in-memory graph needs to get
     * the updated graph from the database.
     */
    public void reinitialize() throws PMException {
        PAP = new PAP();
    }
}
