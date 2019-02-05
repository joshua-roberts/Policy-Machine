package gov.nist.csd.pm.pap;

import gov.nist.csd.pm.common.exceptions.Errors;
import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.Search;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.common.model.prohibitions.ProhibitionsDAO;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.graph.GraphPAP;
import gov.nist.csd.pm.pap.prohibitions.ProhibitionsPAP;
import gov.nist.csd.pm.pap.sessions.SessionManager;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import static gov.nist.csd.pm.common.constants.Operations.ALL_OPERATIONS;
import static gov.nist.csd.pm.common.constants.Properties.NAMESPACE_PROPERTY;
import static gov.nist.csd.pm.common.constants.Properties.PASSWORD_PROPERTY;
import static gov.nist.csd.pm.common.constants.Properties.REP_PROPERTY;
import static gov.nist.csd.pm.common.exceptions.Errors.ERR_HASHING_USER_PSWD;
import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.UA;
import static gov.nist.csd.pm.common.model.graph.nodes.NodeUtils.generatePasswordHash;

/**
 * PAP is the Policy Access Point. The purpose of the PAP is to expose the underlying policy data to the PDP and EPP.
 * It initializes the NGAC backend using the connection properties set through SetConnectionServlet.  This servlet can
 * be access via ../config.jsp upon starting the server.The PAP also stores the in memory graph that will be used for
 * decision making.
 */
public class PAP {

    private int      interval = 30;

    private GraphPAP        graphPAP;
    private ProhibitionsPAP prohibitionsPAP;
    private SessionManager  sessionManager;

    private NodeContext superPC;
    private NodeContext superPCRep;
    private NodeContext superUA1;
    private NodeContext superUA2;
    private NodeContext superU;
    private NodeContext superOA;
    private NodeContext superO;

    private PAP() throws PMException {
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
            throw new PMException(Errors.ERR_LOADING_DB_CONFIG_PROPS, e.getMessage());
        }

        // extract the properties
        String database = props.getProperty("database");
        String host = props.getProperty("host");
        int port = Integer.parseInt(props.getProperty("port"));
        String schema = props.getProperty("schema");
        String username = props.getProperty("username");
        String password = props.getProperty("password");
        String inter = props.getProperty("interval");
        if(inter != null) {
            interval = Integer.parseInt(inter);
        }

        setup(database, host, port, schema, username, password);
    }

    private PAP(String database, String host, int port, String username, String password, String schema, int interval) throws PMException {
        if(interval > 0) {
            this.interval = interval;
        }

        setup(database, host, port, schema, username, password);
    }

    /**
     * Instantiate the backend database, in memory graph, and DAOs. Then, make sure the super user, user attribute,
     * and policy class exist, if not, add them to the graph.  This is done because there must always be a super or root
     * user.
     *
     * @param database The database to use.  Neo4j or MySQL.
     * @param host The name of the database host machine.
     * @param port The port the database is running on.
     * @param schema The database schema if applicable.
     * @param username The username of the database user.
     * @param password The password of the database user
     */
    private void setup(String database, String host, int port, String schema, String username, String password) throws PMException {
        DatabaseContext ctx = new DatabaseContext(database, host, port, username, password, schema);

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
        HashMap<String, String> props = new HashMap<>();
        props.put(NAMESPACE_PROPERTY, "super");

        HashSet<NodeContext> nodes = getGraphPAP().search("super_ua1", UA.toString(), props);
        if(nodes.isEmpty()) {
            long uaID = getGraphPAP().createNode(new NodeContext("super_ua1", UA, props));
            superUA1 = getGraphPAP().getNode(uaID);
        } else {
            superUA1 = nodes.iterator().next();
        }
        nodes = getGraphPAP().search("super_ua2", UA.toString(), props);
        if(nodes.isEmpty()) {
            long uaID = getGraphPAP().createNode(new NodeContext("super_ua2", UA, props));
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
                throw new PMException(ERR_HASHING_USER_PSWD, e.getMessage());
            }
            long uID = getGraphPAP().createNode(new NodeContext("super", NodeType.U, props));
            superU = getGraphPAP().getNode(uID);
            props.remove(PASSWORD_PROPERTY);
        } else {
            superU = nodes.iterator().next();
        }

        nodes = getGraphPAP().search("super", NodeType.OA.toString(), props);
        if(nodes.isEmpty()) {
            long oaID = getGraphPAP().createNode(new NodeContext("super", NodeType.OA, props));
            superOA = getGraphPAP().getNode(oaID);
        } else {
            superOA = nodes.iterator().next();
        }
        nodes = getGraphPAP().search("super", NodeType.O.toString(), props);
        if(nodes.isEmpty()) {
            long oID = getGraphPAP().createNode(new NodeContext("super", NodeType.O, props));
            superO = getGraphPAP().getNode(oID);
        } else {
            superO = nodes.iterator().next();
        }
        nodes = getGraphPAP().search("super rep", NodeType.OA.toString(), props);
        if(nodes.isEmpty()) {
            long repID = getGraphPAP().createNode(new NodeContext("super rep", NodeType.OA, props));
            superPCRep = getGraphPAP().getNode(repID);
        } else {
            superPCRep = nodes.iterator().next();
        }
        nodes = getGraphPAP().search("super", NodeType.PC.toString(), props);
        if(nodes.isEmpty()) {
            // add the rep oa ID to the properties
            props.put(REP_PROPERTY, String.valueOf(superPCRep.getID()));
            long pcID = getGraphPAP().createNode(new NodeContext("super", NodeType.PC, props));
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
        HashSet<NodeContext> children = getGraphPAP().getChildren(superPC.getID());
        if(!children.contains(superUA1)) {
            getGraphPAP().assign(new NodeContext(superUA1.getID(), superUA1.getType()), new NodeContext(superPC.getID(), superPC.getType()));
        }
        // check super ua2 is assigned to super pc
        children = getGraphPAP().getChildren(superPC.getID());
        if(!children.contains(superUA2)) {
            getGraphPAP().assign(new NodeContext(superUA2.getID(), superUA2.getType()), new NodeContext(superPC.getID(), superPC.getType()));
        }
        // check super user is assigned to super ua1
        children = getGraphPAP().getChildren(superUA1.getID());
        if(!children.contains(superU)) {
            getGraphPAP().assign(new NodeContext(superU.getID(), superU.getType()), new NodeContext(superUA1.getID(), superUA1.getType()));
        }
        // check super user is assigned to super ua2
        children = getGraphPAP().getChildren(superUA2.getID());
        if(!children.contains(superU)) {
            getGraphPAP().assign(new NodeContext(superU.getID(), superU.getType()), new NodeContext(superUA2.getID(), superUA2.getType()));
        }
        // check super oa is assigned to super pc
        children = getGraphPAP().getChildren(superPC.getID());
        if(!children.contains(superOA)) {
            getGraphPAP().assign(new NodeContext(superOA.getID(), superOA.getType()), new NodeContext(superPC.getID(), superPC.getType()));
        }
        // check that the super rep is assigned to super oa
        children = getGraphPAP().getChildren(superOA.getID());
        if(!children.contains(superPCRep)) {
            getGraphPAP().assign(new NodeContext(superPCRep.getID(), superPCRep.getType()), new NodeContext(superOA.getID(), superOA.getType()));
        }
        // check super o is assigned to super oa
        if(!children.contains(superO)) {
            getGraphPAP().assign(new NodeContext(superO.getID(), superO.getType()), new NodeContext(superOA.getID(), superOA.getType()));
        }

        // associate super ua to super oa
        getGraphPAP().associate(new NodeContext(superUA2.getID(), UA), new NodeContext(superOA.getID(), superOA.getType()), new HashSet<>(Arrays.asList(ALL_OPERATIONS)));
        getGraphPAP().associate(new NodeContext(superUA1.getID(), UA), new NodeContext(superUA2.getID(), superUA2.getType()), new HashSet<>(Arrays.asList(ALL_OPERATIONS)));
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

    public NodeContext getSuperPC() {
        return superPC;
    }

    public NodeContext getSuperUA1() {
        return superUA1;
    }

    public NodeContext getSuperUA2() {
        return superUA2;
    }

    public NodeContext getSuperU() {
        return superU;
    }

    public NodeContext getSuperOA() {
        return superOA;
    }

    public NodeContext getSuperO() {
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

    /**
     * A static instance of the PAP to be used by the PDP.
     */
    private static PAP PAP;
    public static synchronized PAP getPAP() throws PMException {
        if(PAP == null) {
            PAP = new PAP();
        }
        return PAP;
    }

    public static synchronized PAP getPAP(boolean init, String database, String host, int port, String username, String password, String schema, int interval) throws PMException {
        if(PAP == null || init) {
            PAP = new PAP(database, host, port, username, password, schema, interval);
        }
        return PAP;
    }



    /**
     * Initialize the PAP with the given properties
     * @param props
     * @throws PMException
     */
    public static void init(Properties props) throws PMException {
        //get properties
        String database = props.getProperty("database");
        String host = props.getProperty("host");
        int port = Integer.parseInt(props.getProperty("port"));
        String schema = props.getProperty("schema");
        String username = props.getProperty("username");
        String password = props.getProperty("password");
        String inter = props.getProperty("interval");
        int interval = -1;
        if(inter != null) {
            interval = Integer.parseInt(inter);
        }

        //serialize thr properties
        saveProperties(props);

        //instantiate the PAP
        PAP = new PAP(database, host, port, username, password, schema, interval);
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
}
