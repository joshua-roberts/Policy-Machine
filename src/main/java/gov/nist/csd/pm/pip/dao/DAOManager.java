package gov.nist.csd.pm.pip.dao;

import gov.nist.csd.pm.demos.egrant.ApplicationDAO;
import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidPropertyException;
import gov.nist.csd.pm.pip.model.DatabaseContext;
import gov.nist.csd.pm.pip.dao.neo4j.*;
import gov.nist.csd.pm.pip.dao.sql.*;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DAOManager {

    private GraphDAO        graphDAO;
    private NodesDAO        nodesDAO;
    private AssignmentsDAO  assignmentsDAO;
    private AssociationsDAO associationsDAO;
    private ObligationsDAO  obligationsDAO;
    private ProhibitionsDAO prohibitionsDAO;
    private SessionsDAO     sessionsDAO;
    private ApplicationDAO  applicationDAO;

    private String database;
    private String host;
    private int port;
    private String username;
    private String password;
    private String schema;
    private int interval = 30;

    public Connection connection;

    private DAOManager() throws IOException, ClassNotFoundException, DatabaseException, SQLException, InvalidPropertyException {
        //deserialize
        FileInputStream fis = new FileInputStream("pm.conf");
        ObjectInputStream ois = new ObjectInputStream(fis);
        Properties props = (Properties) ois.readObject();

        //get properties
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

        DatabaseContext ctx = new DatabaseContext(host, port, username, password, schema);

        if(database.equalsIgnoreCase("neo4j")) {
            nodesDAO = new Neo4jNodesDAO(ctx);
            assignmentsDAO = new Neo4jAssignmentsDAO(ctx);
            associationsDAO = new Neo4jAssociationsDAO(ctx);
            obligationsDAO = new Neo4jObligationsDAO(ctx);
            prohibitionsDAO = new Neo4jProhibitionsDAO(ctx);
            sessionsDAO = new Neo4jSessionsDAO(ctx);
            graphDAO = new Neo4jGraphDAO(ctx);
            applicationDAO = new ApplicationDAO(ctx);
        } else {
            nodesDAO = new SqlNodesDAO(ctx);
            assignmentsDAO = new SqlAssignmentsDAO(ctx);
            associationsDAO = new SqlAssociationsDAO(ctx);
            obligationsDAO = new SqlObligationsDAO(ctx);
            prohibitionsDAO = new SqlProhibitionsDAO(ctx);
            sessionsDAO = new SqlSessionsDAO(ctx);
            graphDAO = new SqlGraphDAO(ctx);
            applicationDAO = new ApplicationDAO(ctx);
        }

        System.out.println("DAO initialized");
    }

    private DAOManager(String database, String host, int port, String username, String password, String schema, int interval) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        this.database = database;
        this.host = host;
        this.port = port;
        this.schema = schema;
        this.username = username;
        this.password = password;
        if(interval > 0) {
            this.interval = interval;
        }

        DatabaseContext ctx = new DatabaseContext(host, port, username, password, schema);

        if(database.equalsIgnoreCase("neo4j")) {
            nodesDAO = new Neo4jNodesDAO(ctx);
            assignmentsDAO = new Neo4jAssignmentsDAO(ctx);
            associationsDAO = new Neo4jAssociationsDAO(ctx);
            obligationsDAO = new Neo4jObligationsDAO(ctx);
            prohibitionsDAO = new Neo4jProhibitionsDAO(ctx);
            sessionsDAO = new Neo4jSessionsDAO(ctx);
            graphDAO = new Neo4jGraphDAO(ctx);
            applicationDAO = new ApplicationDAO(ctx);
        } else {
            /*nodesDAO = new SqlNodesDAO(connection);
            assignmentsDAO = new SqlAssignmentsDAO(connection);
            associationsDAO = new SqlAssociationsDAO(connection);
            obligationsDAO = new SqlObligationsDAO(connection);
            prohibitionsDAO = new SqlProhibitionsDAO(connection);
            sessionsDAO = new SqlSessionsDAO(connection);
            graphDAO = new SqlGraphDAO(connection);
            applicationDAO = new ApplicationDAO();

            System.out.println("DAO initialized");*/
        }
    }

    public GraphDAO getGraphDAO() {
        return graphDAO;
    }

    public NodesDAO getNodesDAO() {
        return nodesDAO;
    }

    public AssignmentsDAO getAssignmentsDAO() {
        return assignmentsDAO;
    }

    public AssociationsDAO getAssociationsDAO() {
        return associationsDAO;
    }

    public ObligationsDAO getObligationsDAO() {
        return obligationsDAO;
    }

    public ProhibitionsDAO getProhibitionsDAO() {
        return prohibitionsDAO;
    }

    public SessionsDAO getSessionsDAO() {
        return sessionsDAO;
    }

    public ApplicationDAO getApplicationDAO() {
        return applicationDAO;
    }


    private static DAOManager daoManager;
    public static DAOManager getDaoManager() throws IOException, ClassNotFoundException, DatabaseException, SQLException, InvalidPropertyException {
        if(daoManager == null) {
            synchronized(DAOManager.class) {
                if (daoManager == null) {
                    daoManager = new DAOManager();
                }
            }
        }
        return daoManager;
    }

    public static void init(Properties props) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
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

        daoManager = new DAOManager(database, host, port, username, password, schema, interval);
    }

    private static void saveProperties(Properties props) {
        try {
            FileOutputStream fos = new FileOutputStream("pm.conf");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(props);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
