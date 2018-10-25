package gov.nist.csd.pm.pip.db.neo4j;

import gov.nist.csd.pm.model.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_DB;

/**
 * Object that sotres a connectino to a neo4j database.
 */
public class Neo4jConnection {

    private String host;
    private int port;
    private String username;
    private String password;

    /**
     * Establishes a new connection to a Neo4j database.
     * @param host The hostname of the Neo4j instance.
     * @param port The port the Neo4j instance is running on.
     * @param username The name of the Neo4j user.
     * @param password The password of the Neo4j user.
     * @throws DatabaseException When there's ann error connecting to the Neo4j instance.
     */
    public Neo4jConnection(String host, int port, String username, String password) throws DatabaseException {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;

        try {
            Driver driver = new org.neo4j.jdbc.Driver();
            DriverManager.registerDriver(driver);
        } catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    /**
     * @return The connection to the Neo4j instance.
     */
    public Connection getConnection() throws DatabaseException {
        try {
            return DriverManager.getConnection("jdbc:neo4j:http://" + host + ":" + port + "", username, password);
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }
}
