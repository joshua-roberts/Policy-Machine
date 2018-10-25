package gov.nist.csd.pm.pip;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.LoadConfigException;
import gov.nist.csd.pm.pip.graph.NGACMem;
import gov.nist.csd.pm.model.graph.NGAC;
import gov.nist.csd.pm.pip.graph.NGACNeo4j;
import gov.nist.csd.pm.pip.graph.NGACSQL;
import gov.nist.csd.pm.pip.loader.LoaderException;
import gov.nist.csd.pm.pip.loader.Neo4jLoader;
import gov.nist.csd.pm.pip.loader.SQLLoader;
import gov.nist.csd.pm.pip.model.DatabaseContext;
import gov.nist.csd.pm.pip.model.NGACBackend;
import gov.nist.csd.pm.model.graph.Search;
import gov.nist.csd.pm.pip.search.Neo4jSearch;
import gov.nist.csd.pm.pip.search.SQLSearch;
import gov.nist.csd.pm.pip.sessions.Neo4jSessionsDAO;
import gov.nist.csd.pm.pip.sessions.SQLSessionsDAO;
import gov.nist.csd.pm.pip.sessions.SessionsDAO;

import java.io.*;
import java.util.Properties;

/**
 * PIP is the Policy Information Point. It initializes the NGAC backend using the connection properties set through
 * SetConnectionServlet.  This servlet can be access via ../config.jsp upon starting the server.
 * The PIP also stores the in memory graph that will be used for decision making.
 */
public class PIP {

    private String   database;
    private String   host;
    private int      port;
    private String   username;
    private String   password;
    private String   schema;
    private int      interval = 30;

    private NGACBackend backend;
    private SessionsDAO sessionsDAO;

    private PIP() throws LoadConfigException, DatabaseException, LoaderException {
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
        database = props.getProperty("database");
        host = props.getProperty("host");
        port = Integer.parseInt(props.getProperty("port"));
        schema = props.getProperty("schema");
        username = props.getProperty("username");
        password = props.getProperty("password");
        String inter = props.getProperty("interval");
        if(inter != null) {
            interval = Integer.parseInt(inter);
        }

        setupBackend();
    }

    private PIP(String database, String host, int port, String username, String password, String schema, int interval) throws DatabaseException, LoaderException {
        this.database = database;
        this.host = host;
        this.port = port;
        this.schema = schema;
        this.username = username;
        this.password = password;
        if(interval > 0) {
            this.interval = interval;
        }

        setupBackend();
    }

    /**
     * Instantiate the backend database, in memory graph, and DAOs.
     */
    private void setupBackend() throws DatabaseException, LoaderException {
        DatabaseContext ctx = new DatabaseContext(host, port, username, password, schema);

        NGAC db;
        NGAC mem;
        Search search;
        if(database.equalsIgnoreCase("neo4j")) {
            db = new NGACNeo4j(ctx);
            search = new Neo4jSearch(ctx);
            mem = new NGACMem(new Neo4jLoader(ctx));
            sessionsDAO = new Neo4jSessionsDAO(ctx);
        } else {
            db = new NGACSQL(ctx);
            search = new SQLSearch(ctx);
            mem = new NGACMem(new SQLLoader(ctx));
            sessionsDAO = new SQLSessionsDAO(ctx);
        }

        this.backend = new NGACBackend(db, mem, search);
    }

    public NGACBackend getNGACBackend() {
        return backend;
    }

    public SessionsDAO getSessionsDAO() {
        return sessionsDAO;
    }

    /**
     * A static instance of the PIP to be used by the PDP.
     */
    private static PIP pip;
    public static synchronized PIP getPIP() throws DatabaseException, LoadConfigException, LoaderException {
        if(pip == null) {
            pip = new PIP();
        }
        return pip;
    }

    /**
     * Initialize the PIP with the given properties
     * @param props
     * @throws DatabaseException
     */
    public static void init(Properties props) throws DatabaseException, LoaderException {
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

        //instantiate the PIP
        pip = new PIP(database, host, port, username, password, schema, interval);
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
