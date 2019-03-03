package gov.nist.csd.pm.pap;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.util.NodeUtils;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.graph.model.nodes.NodeType;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.graph.GraphPAP;
import gov.nist.csd.pm.pap.prohibitions.ProhibitionsPAP;
import gov.nist.csd.pm.pap.sessions.SessionManager;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static gov.nist.csd.pm.common.constants.Operations.ALL_OPERATIONS;
import static gov.nist.csd.pm.common.constants.Properties.*;
import static gov.nist.csd.pm.common.util.NodeUtils.generatePasswordHash;
import static gov.nist.csd.pm.graph.model.nodes.NodeType.UA;

/**
 * PAP is the Policy Access Point. The purpose of the PAP is to expose the underlying policy data to the PDP and EPP.
 * It initializes the backend using the connection properties set through SetConnectionServlet.  This servlet can
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
    public static synchronized PAP getPAP(DatabaseContext ctx) throws PMException {
        PAP = new PAP(ctx);
        return PAP;
    }

    /**
     * Save the configuration properties in a file, to be read upon server startup.
     * @param props A properties object containing the server configuration settings.
     */
    private static void saveProperties(Properties props) {
        try (FileOutputStream fos = new FileOutputStream("pm.conf");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(props);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PAP() throws PMException {
        System.out.println("initializing pap");
        // deserialize the configuration properties
        FileInputStream fis;
        Properties props;
        try {
            fis = new FileInputStream("pm.conf");
            try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                props = (Properties) ois.readObject();
            }
        }
        catch (IOException | ClassNotFoundException e) {
            throw new PMConfigurationException(e.getMessage());
        }

        // extract the properties
        String database = props.getProperty("database");
        String host = props.getProperty("host");
        int port = Integer.parseInt(props.getProperty("port"));
        String schema = props.getProperty("schema");
        String username = props.getProperty("username");
        String password = props.getProperty("password");

        init(new DatabaseContext(DatabaseContext.toEnum(database), host, port, username, password, schema));
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
        graphPAP = new GraphPAP(ctx);
        prohibitionsPAP = new ProhibitionsPAP(ctx);
        sessionManager = new SessionManager();

        // check that the super nodes are created
        checkMetadata();
    }

    /**
     * Check that all of the super meta data is in the graph.  Create any nodes, assignments, or associations that do not
     * exist but should
     */
    private void checkMetadata() throws PMException {
        HashMap<String, String> props = NodeUtils.toProperties(NAMESPACE_PROPERTY, "super");

        Set<Node> nodes = getGraphPAP().search("super_ua1", UA.toString(), props);
        if(nodes.isEmpty()) {
            long uaID = getGraphPAP().createNode(new Node("super_ua1", UA, props));
            superUA1 = getGraphPAP().getNode(uaID);
        } else {
            superUA1 = nodes.iterator().next();
        }
        nodes = getGraphPAP().search("super_ua2", UA.toString(), props);
        if(nodes.isEmpty()) {
            long uaID = getGraphPAP().createNode(new Node("super_ua2", UA, props));
            superUA2 = getGraphPAP().getNode(uaID);
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
            long uID = getGraphPAP().createNode(new Node("super", NodeType.U, props));
            superU = getGraphPAP().getNode(uID);
            props.remove(PASSWORD_PROPERTY);
        } else {
            superU = nodes.iterator().next();
        }

        nodes = getGraphPAP().search("super", NodeType.OA.toString(), props);
        if(nodes.isEmpty()) {
            long oaID = getGraphPAP().createNode(new Node("super", NodeType.OA, props));
            superOA = getGraphPAP().getNode(oaID);
        } else {
            superOA = nodes.iterator().next();
        }
        nodes = getGraphPAP().search("super", NodeType.O.toString(), props);
        if(nodes.isEmpty()) {
            long oID = getGraphPAP().createNode(new Node("super", NodeType.O, props));
            superO = getGraphPAP().getNode(oID);
        } else {
            superO = nodes.iterator().next();
        }
        nodes = getGraphPAP().search("super rep", NodeType.OA.toString(), props);
        if(nodes.isEmpty()) {
            long repID = getGraphPAP().createNode(new Node("super rep", NodeType.OA, props));
            superPCRep = getGraphPAP().getNode(repID);
        } else {
            superPCRep = nodes.iterator().next();
        }
        nodes = getGraphPAP().search("super", NodeType.PC.toString(), props);
        if(nodes.isEmpty()) {
            // add the rep oa ID to the properties
            props.put(REP_PROPERTY, String.valueOf(superPCRep.getID()));
            long pcID = getGraphPAP().createNode(new Node("super", NodeType.PC, props));
            superPC = getGraphPAP().getNode(pcID);
            props.remove(REP_PROPERTY);
        } else {
            superPC = nodes.iterator().next();

            // make sure the rep ID property is correct
            // if the rep Id for the pc node is null, empty, or doesn't match the current rep ID, update the ID
            String repID = superPC.getProperties().get(REP_PROPERTY);
            if(repID == null || repID.isEmpty() || !repID.equals(String.valueOf(superPCRep.getID()))) {
                repID = String.valueOf(superPCRep.getID());
                superPC.getProperties().put(REP_PROPERTY, repID);
                // update the node
                getGraphPAP().updateNode(superPC);
            }
        }

        // check super ua1 is assigned to super pc
        Set<Long> children = getGraphPAP().getChildren(superPC.getID());
        if(!children.contains(superUA1.getID())) {
            getGraphPAP().assign(new Node(superUA1.getID(), superUA1.getType()), new Node(superPC.getID(), superPC.getType()));
        }
        // check super ua2 is assigned to super pc
        children = getGraphPAP().getChildren(superPC.getID());
        if(!children.contains(superUA2.getID())) {
            getGraphPAP().assign(new Node(superUA2.getID(), superUA2.getType()), new Node(superPC.getID(), superPC.getType()));
        }
        // check super user is assigned to super ua1
        children = getGraphPAP().getChildren(superUA1.getID());
        if(!children.contains(superU.getID())) {
            getGraphPAP().assign(new Node(superU.getID(), superU.getType()), new Node(superUA1.getID(), superUA1.getType()));
        }
        // check super user is assigned to super ua2
        children = getGraphPAP().getChildren(superUA2.getID());
        if(!children.contains(superU.getID())) {
            getGraphPAP().assign(new Node(superU.getID(), superU.getType()), new Node(superUA2.getID(), superUA2.getType()));
        }
        // check super oa is assigned to super pc
        children = getGraphPAP().getChildren(superPC.getID());
        if(!children.contains(superOA.getID())) {
            getGraphPAP().assign(new Node(superOA.getID(), superOA.getType()), new Node(superPC.getID(), superPC.getType()));
        }
        // check that the super rep is assigned to super oa
        children = getGraphPAP().getChildren(superOA.getID());
        if(!children.contains(superPCRep.getID())) {
            getGraphPAP().assign(new Node(superPCRep.getID(), superPCRep.getType()), new Node(superOA.getID(), superOA.getType()));
        }
        // check super o is assigned to super oa
        if(!children.contains(superO.getID())) {
            getGraphPAP().assign(new Node(superO.getID(), superO.getType()), new Node(superOA.getID(), superOA.getType()));
        }

        // associate super ua to super oa
        getGraphPAP().associate(new Node(superUA2.getID(), UA), new Node(superOA.getID(), superOA.getType()), new HashSet<>(Arrays.asList(ALL_OPERATIONS)));
        getGraphPAP().associate(new Node(superUA1.getID(), UA), new Node(superUA2.getID(), superUA2.getType()), new HashSet<>(Arrays.asList(ALL_OPERATIONS)));
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

    /**
     * Reinitialize the PAP.  Set up the NGAC backend.  This is primarily used when loading a configuration.  Loading
     * a configuration makes changes to the database but not the in memory graph.  So, the in-memory graph needs to get
     * the updated graph from the database.
     */
    public void reinitialize() throws PMException {
        PAP = new PAP();
    }
}
