package gov.nist.csd.pm.pap;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidProhibitionSubjectTypeException;
import gov.nist.csd.pm.model.exceptions.LoadConfigException;
import gov.nist.csd.pm.model.graph.Graph;
import gov.nist.csd.pm.pap.graph.MemGraph;
import gov.nist.csd.pm.pap.graph.Neo4jGraph;
import gov.nist.csd.pm.pap.graph.SQLGraph;
import gov.nist.csd.pm.model.exceptions.LoaderException;
import gov.nist.csd.pm.pap.loader.graph.Neo4jGraphLoader;
import gov.nist.csd.pm.pap.loader.graph.SQLGraphLoader;
import gov.nist.csd.pm.pap.loader.prohibitions.Neo4jProhibitionsLoader;
import gov.nist.csd.pm.pap.loader.prohibitions.SQLProhibitionsLoader;
import gov.nist.csd.pm.pap.loader.sessions.Neo4jSessionsLoader;
import gov.nist.csd.pm.pap.loader.sessions.SQLSessionsLoader;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.model.graph.Search;
import gov.nist.csd.pm.pap.prohibitions.MemProhibitionsDAO;
import gov.nist.csd.pm.pap.prohibitions.Neo4jProhibitionsDAO;
import gov.nist.csd.pm.model.prohibitions.ProhibitionsDAO;
import gov.nist.csd.pm.pap.prohibitions.SQLProhibitionsDAO;
import gov.nist.csd.pm.pap.search.Neo4jSearch;
import gov.nist.csd.pm.pap.search.SQLSearch;
import gov.nist.csd.pm.pap.sessions.MemSessionsDAO;
import gov.nist.csd.pm.pap.sessions.Neo4jSessionsDAO;
import gov.nist.csd.pm.pap.sessions.SQLSessionsDAO;
import gov.nist.csd.pm.pap.sessions.SessionsDAO;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;

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

    private PAP() throws LoadConfigException, DatabaseException, LoaderException, InvalidProhibitionSubjectTypeException {
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
            throw new LoadConfigException();
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

    private PAP(String database, String host, int port, String username, String password, String schema, int interval) throws DatabaseException, LoaderException, InvalidProhibitionSubjectTypeException {
        if(interval > 0) {
            this.interval = interval;
        }

        setup(database, host, port, schema, username, password);
    }

    /**
     * Instantiate the backend database, in memory graph, and DAOs.
     * @param database The database to use.  Neo4j or MySQL.
     * @param host The name of the database host machine.
     * @param port The port the database is running on.
     * @param schema The database schema if applicable.
     * @param username The username of the database user.
     * @param password The password of the database user
     */
    private void setup(String database, String host, int port, String schema, String username, String password) throws DatabaseException, LoaderException, InvalidProhibitionSubjectTypeException {
        DatabaseContext ctx = new DatabaseContext(host, port, username, password, schema);

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

    /**
     * Reinitialize the PAP.  Set up the NGAC backend.  This is primarily used when loading a configuration.  Loading
     * a configuration makes changes to the database but not the in memory graph.  So, the in-memory graph needs to get
     * the updated graph from the database.
     */
    public void reinitialize() throws DatabaseException, LoadConfigException, LoaderException, InvalidProhibitionSubjectTypeException {
        PAP = new PAP();
    }

    public HashMap<String, Graph> graphs = new HashMap<>();
    public HashMap<String, Graph> getGraphs() {
        return graphs;
    }

    /**
     * A static instance of the PAP to be used by the PDP.
     */
    private static PAP PAP;
    public static synchronized PAP getPAP() throws DatabaseException, LoadConfigException, LoaderException, InvalidProhibitionSubjectTypeException {
        if(PAP == null) {
            PAP = new PAP();
        }
        return PAP;
    }

    /**
     * Initialize the PAP with the given properties
     * @param props
     * @throws DatabaseException
     */
    public static void init(Properties props) throws DatabaseException, LoaderException, InvalidProhibitionSubjectTypeException {
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
