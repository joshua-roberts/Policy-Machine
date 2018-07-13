package gov.nist.policyserver.dao;

import gov.nist.policyserver.dao.neo4j.*;
import gov.nist.policyserver.dao.sql.*;
import gov.nist.policyserver.exceptions.DatabaseException;

import java.io.*;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static gov.nist.policyserver.common.Constants.ERR_NEO;

public class DAOManager {

    public static GraphDAO graphDAO;
    public static NodesDAO nodesDAO;
    public static AssignmentsDAO assignmentsDAO;
    public static AssociationsDAO associationsDAO;
    public static ObligationsDAO obligationsDAO;
    public static ProhibitionsDAO prohibitionsDAO;
    public static SessionsDAO sessionsDAO;

    private String database;
    private String host;
    private int port;
    private String username;
    private String password;
    private String schema;
    private int interval = 30;

    public Connection connection;
    public static DAOManager instance = new DAOManager();

    public DAOManager(){
    }

    public void Initialize() throws IOException, ClassNotFoundException, DatabaseException, SQLException {
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
            if (inter != null) {
                interval = Integer.parseInt(inter);
            }

            if (database.equalsIgnoreCase("neo4j")) {
                neo4jConnect();

                nodesDAO = new Neo4jNodesDAO(connection);
                assignmentsDAO = new Neo4jAssignmentsDAO(connection);
                associationsDAO = new Neo4jAssociationsDAO(connection);
                obligationsDAO = new Neo4jObligationsDAO(connection);
                prohibitionsDAO = new Neo4jProhibitionsDAO(connection);
                sessionsDAO = new Neo4jSessionsDAO(connection);
                graphDAO = new Neo4jGraphDAO(connection);
            } else {
                sqlConnect();
                nodesDAO = new SqlNodesDAO(connection);
                assignmentsDAO = new SqlAssignmentsDAO(connection);
                associationsDAO = new SqlAssociationsDAO(connection);
                obligationsDAO = new SqlObligationsDAO(connection);
                prohibitionsDAO = new SqlProhibitionsDAO(connection);
                sessionsDAO = new SqlSessionsDAO(connection);
                graphDAO = new SqlGraphDAO(connection);
            }
    }

    private DAOManager(String database, String host, int port, String username, String password, String schema, int interval) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        this.database = database;
        this.host = host;
        this.port = port;
        this.schema = schema;
        this.username = username;
        this.password = password;
        if(interval > 0) {
            this.interval = interval;
        }

        if(database.equalsIgnoreCase("neo4j")) {
            neo4jConnect();

            graphDAO = new Neo4jGraphDAO(connection);
            nodesDAO = new Neo4jNodesDAO(connection);
            assignmentsDAO = new Neo4jAssignmentsDAO(connection);
            associationsDAO = new Neo4jAssociationsDAO(connection);
            obligationsDAO = new Neo4jObligationsDAO(connection);
            prohibitionsDAO = new Neo4jProhibitionsDAO(connection);
            sessionsDAO = new Neo4jSessionsDAO(connection);
        } else {
            sqlConnect();

            graphDAO = new SqlGraphDAO(connection);
            nodesDAO = new SqlNodesDAO(connection);
            assignmentsDAO = new SqlAssignmentsDAO(connection);
            associationsDAO = new SqlAssociationsDAO(connection);
            obligationsDAO = new SqlObligationsDAO(connection);
            prohibitionsDAO = new SqlProhibitionsDAO(connection);
            sessionsDAO = new SqlSessionsDAO(connection);

            System.out.println("DAO initialized");
        }
    }

    public void neo4jConnect() throws DatabaseException {
        try {
            Driver driver = new org.neo4j.jdbc.Driver();
            DriverManager.registerDriver(driver);
            connection = DriverManager.getConnection("jdbc:neo4j:http://" + host + ":" + port + "", username, password);

            //load nodes into cache
            //warmUp();

        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    private void sqlConnect() throws DatabaseException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + schema, username, password);
        }catch(Exception e){
            throw new DatabaseException(e.hashCode(), e.getMessage());
        }
    }

    public static GraphDAO getGraphDAO() {
        return graphDAO;
    }

    public static NodesDAO getNodesDAO() {
        return nodesDAO;
    }

    public static AssignmentsDAO getAssignmentsDAO() {
        return assignmentsDAO;
    }

    public static AssociationsDAO getAssociationsDAO() {
        return associationsDAO;
    }

    public static ObligationsDAO getObligationsDAO() {
        return obligationsDAO;
    }

    public static ProhibitionsDAO getProhibitionsDAO() {
        return prohibitionsDAO;
    }

    public static SessionsDAO getSessionsDAO() {
        return sessionsDAO;
    }

//    public static DAOManager daoManager;

    public static DAOManager getDaoManager() {
        return instance;
    }

    public static void init(Properties props) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
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

        if(instance == null) {
            instance = new DAOManager(database, host, port, username, password, schema, interval);
        }

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
