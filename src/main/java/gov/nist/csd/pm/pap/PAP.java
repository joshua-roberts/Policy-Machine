package gov.nist.csd.pm.pap;

import gov.nist.csd.pm.common.exceptions.Errors;
import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.Search;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.common.model.prohibitions.ProhibitionsDAO;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.graph.MemGraph;
import gov.nist.csd.pm.pap.graph.Neo4jGraph;
import gov.nist.csd.pm.pap.graph.SQLGraph;
import gov.nist.csd.pm.pap.loader.graph.Neo4jGraphLoader;
import gov.nist.csd.pm.pap.loader.graph.SQLGraphLoader;
import gov.nist.csd.pm.pap.loader.prohibitions.Neo4jProhibitionsLoader;
import gov.nist.csd.pm.pap.loader.prohibitions.SQLProhibitionsLoader;
import gov.nist.csd.pm.pap.loader.sessions.Neo4jSessionsLoader;
import gov.nist.csd.pm.pap.loader.sessions.SQLSessionsLoader;
import gov.nist.csd.pm.pap.prohibitions.MemProhibitionsDAO;
import gov.nist.csd.pm.pap.prohibitions.Neo4jProhibitionsDAO;
import gov.nist.csd.pm.pap.prohibitions.SQLProhibitionsDAO;
import gov.nist.csd.pm.pap.search.Neo4jSearch;
import gov.nist.csd.pm.pap.search.SQLSearch;
import gov.nist.csd.pm.pap.sessions.MemSessionsDAO;
import gov.nist.csd.pm.pap.sessions.Neo4jSessionsDAO;
import gov.nist.csd.pm.pap.sessions.SQLSessionsDAO;
import gov.nist.csd.pm.pap.sessions.SessionsDAO;

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
import static gov.nist.csd.pm.common.exceptions.Errors.ERR_DB;
import static gov.nist.csd.pm.common.exceptions.Errors.ERR_HASHING_USER_PSWD;
import static gov.nist.csd.pm.common.model.graph.nodes.Node.generatePasswordHash;

/**
 * PAP is the Policy Access Point. The purpose of the PA is to expose the underlying policy data to the PDP and EPP.
 * It initializes the NGAC backend using the connection properties set through. SetConnectionServlet.  This servlet can
 * be access via ../config.jsp upon starting the server.The PAP also stores the in memory graph that will be used for
 * decision making.
 */
public class PAP {

    private int      interval = 30;

    private Graph           graphDB;
    private Graph           graphMem;
    private Search          search;
    private ProhibitionsDAO prohibitionsDB;
    private ProhibitionsDAO prohibitionsMem;
    private SessionsDAO     sessionsDB;
    private SessionsDAO     sessionsMem;

    private Node superPC;
    private Node superUA1;
    private Node superUA2;
    private Node superU;
    private Node superOA;
    private Node superO;

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
     * user who can create policy classes.
     *
     * @param database The database to use.  Neo4j or MySQL.
     * @param host The name of the database host machine.
     * @param port The port the database is running on.
     * @param schema The database schema if applicable.
     * @param username The username of the database user.
     * @param password The password of the database user
     */
    private void setup(String database, String host, int port, String schema, String username, String password) throws PMException {
        DatabaseContext ctx = new DatabaseContext(host, port, username, password, schema);

        //instantiate PAP fields according to the selected database
        if(database.equalsIgnoreCase("neo4j")) {
            graphDB = new Neo4jGraph(ctx);
            search = new Neo4jSearch(ctx);
            graphMem = new MemGraph(new Neo4jGraphLoader(ctx));
            prohibitionsDB = new Neo4jProhibitionsDAO(ctx);
            prohibitionsMem = new MemProhibitionsDAO(new Neo4jProhibitionsLoader(ctx));
            sessionsDB = new Neo4jSessionsDAO(ctx);
            sessionsMem = new MemSessionsDAO(new Neo4jSessionsLoader(ctx));
        } else {
            graphDB = new SQLGraph(ctx);
            search = new SQLSearch(ctx);
            graphMem = new MemGraph(new SQLGraphLoader(ctx));
            prohibitionsDB = new SQLProhibitionsDAO(ctx);
            prohibitionsMem = new MemProhibitionsDAO(new SQLProhibitionsLoader(ctx));
            sessionsDB = new SQLSessionsDAO(ctx);
            sessionsMem = new MemSessionsDAO(new SQLSessionsLoader(ctx));
        }

        // check that the super nodes are created
        checkMetadata();
    }

    private void checkMetadata() throws PMException {
        HashMap<String, String> props = new HashMap<>();
        props.put(NAMESPACE_PROPERTY, "super");

        HashSet<Node> nodes = search.search("super", NodeType.PC.toString(), props);
        if(nodes.isEmpty()) {
            long pcID = graphDB.createNode(new Node("super", NodeType.PC, props));
            superPC = search.getNode(pcID);
        } else {
            superPC = nodes.iterator().next();
        }
        nodes = search.search("super_ua1", NodeType.UA.toString(), props);
        if(nodes.isEmpty()) {
            long uaID = graphDB.createNode(new Node("super_ua1", NodeType.UA, props));
            superUA1 = search.getNode(uaID);
        } else {
            superUA1 = nodes.iterator().next();
        }
        nodes = search.search("super_ua2", NodeType.UA.toString(), props);
        if(nodes.isEmpty()) {
            long uaID = graphDB.createNode(new Node("super_ua2", NodeType.UA, props));
            superUA2 = search.getNode(uaID);
        } else {
            superUA2 = nodes.iterator().next();
        }
        nodes = search.search("super", NodeType.U.toString(), props);
        if(nodes.isEmpty()) {
            try {
                props.put(PASSWORD_PROPERTY, generatePasswordHash("super"));
            }
            catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new PMException(ERR_HASHING_USER_PSWD, e.getMessage());
            }
            long uID = graphDB.createNode(new Node("super", NodeType.U, props));
            superU = search.getNode(uID);
        } else {
            superU = nodes.iterator().next();
        }
        nodes = search.search("super", NodeType.OA.toString(), props);
        if(nodes.isEmpty()) {
            long oaID = graphDB.createNode(new Node("super", NodeType.OA, props));
            superOA = search.getNode(oaID);
        } else {
            superOA = nodes.iterator().next();
        }
        nodes = search.search("super", NodeType.O.toString(), props);
        if(nodes.isEmpty()) {
            long oID = graphDB.createNode(new Node("super", NodeType.O, props));
            superO = search.getNode(oID);
        } else {
            superO = nodes.iterator().next();
        }

        // check super ua1 is assigned to super pc
        HashSet<Node> children = graphDB.getChildren(superPC.getID());
        if(!children.contains(superUA1)) {
            graphDB.assign(superUA1.getID(), superUA1.getType(), superPC.getID(), superPC.getType());
        }
        // check super ua2 is assigned to super pc
        children = graphDB.getChildren(superPC.getID());
        if(!children.contains(superUA2)) {
            graphDB.assign(superUA2.getID(), superUA2.getType(), superPC.getID(), superPC.getType());
        }
        // check super user is assigned to super ua1
        children = graphDB.getChildren(superUA1.getID());
        if(!children.contains(superU)) {
            graphDB.assign(superU.getID(), superU.getType(), superUA1.getID(), superUA1.getType());
        }
        // check super user is assigned to super ua2
        children = graphDB.getChildren(superUA2.getID());
        if(!children.contains(superU)) {
            graphDB.assign(superU.getID(), superU.getType(), superUA2.getID(), superUA2.getType());
        }
        // check super oa is assigned to super pc
        children = graphDB.getChildren(superPC.getID());
        if(!children.contains(superOA)) {
            graphDB.assign(superOA.getID(), superOA.getType(), superPC.getID(), superPC.getType());
        }
        // check super o is assigned to super oa
        children = graphDB.getChildren(superOA.getID());
        if(!children.contains(superO)) {
            graphDB.assign(superO.getID(), superO.getType(), superOA.getID(), superOA.getType());
        }

        // associate super ua to super oa
        graphDB.associate(superUA1.getID(), superOA.getID(), superOA.getType(), new HashSet<>(Arrays.asList(ALL_OPERATIONS)));
        graphDB.associate(superUA1.getID(), superUA2.getID(), superUA2.getType(), new HashSet<>(Arrays.asList(ALL_OPERATIONS)));

    }

    public Graph getGraphDB() {
        return graphDB;
    }

    public Graph getGraphMem() {
        return graphMem;
    }

    public Search getSearch() {
        return search;
    }

    public ProhibitionsDAO getProhibitionsDB() {
        return prohibitionsDB;
    }

    public ProhibitionsDAO getProhibitionsMem() {
        return prohibitionsMem;
    }

    public SessionsDAO getSessionsDB() {
        return sessionsDB;
    }

    public SessionsDAO getSessionsMem() {
        return sessionsMem;
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
