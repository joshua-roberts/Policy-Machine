package gov.nist.policyserver.dao;

import gov.nist.policyserver.dao.neo4j.*;
import gov.nist.policyserver.dao.sql.*;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.graph.PmGraph;

import java.io.*;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static gov.nist.policyserver.common.Constants.ERR_NEO;

public class DAOManager {

    private GraphDAO        graphDAO;
    private NodesDAO        nodesDAO;
    private AssignmentsDAO  assignmentsDAO;
    private AssociationsDAO associationsDAO;
    private ObligationsDAO  obligationsDAO;
    private ProhibitionsDAO prohibitionsDAO;
    private SessionsDAO     sessionsDAO;

    String database;
    String host;
    int    port;
    String username;
    String password;
    String schema;
    int interval = 30;

    private Connection connection;

    public DAOManager() throws IOException, ClassNotFoundException, DatabaseException, SQLException {
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

            graphDAO = new SqlGraphDAO();
            nodesDAO = new SqlNodesDAO();
            assignmentsDAO = new SqlAssignmentsDAO();
            associationsDAO = new SqlAssociationsDAO();
            obligationsDAO = new SqlObligationsDAO();
            prohibitionsDAO = new SqlProhibitionsDAO();
            sessionsDAO = new SqlSessionsDAO();
        }

        System.out.println("DAO initialized");
    }

    public DAOManager(String database, String host, int port, String username, String password, String schema, int interval) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
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

            graphDAO = new SqlGraphDAO();
            nodesDAO = new SqlNodesDAO();
            assignmentsDAO = new SqlAssignmentsDAO();
            associationsDAO = new SqlAssociationsDAO();
            obligationsDAO = new SqlObligationsDAO();
            prohibitionsDAO = new SqlProhibitionsDAO();
            sessionsDAO = new SqlSessionsDAO();
        }
    }

    public void neo4jConnect() throws DatabaseException {
        try {
            Driver driver = new org.neo4j.jdbc.Driver();
            DriverManager.registerDriver(driver);
            connection = DriverManager.getConnection("jdbc:neo4j:http://" + host + ":" + port + "", username, password);

            //load nodes into cache
            //warmUp();

            System.out.println("Connected to Neo4j");
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    public void sqlConnect() throws DatabaseException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + schema, username, password);
            System.out.println("Connected to MySQL");
        }catch(Exception e){
            throw new DatabaseException(e.hashCode(), e.getMessage());
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

    private static DAOManager daoManager;
    public static DAOManager getDaoManager() throws IOException, ClassNotFoundException, DatabaseException, SQLException {
        if(daoManager == null) {
            daoManager = new DAOManager();
        }
        return daoManager;
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
