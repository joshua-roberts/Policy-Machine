package gov.nist.csd.pm.pip.dao.sql;

import gov.nist.csd.pm.model.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;

import static gov.nist.csd.pm.pep.response.ApiResponseCodes.ERR_MYSQL;

public class SQLConnection {

    private Connection connection;

    public SQLConnection(String host, int port, String username, String password, String db) throws DatabaseException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db, username, password);
        } catch (SQLException | ClassNotFoundException e) {
            throw new DatabaseException(ERR_MYSQL, e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public static String setToString(HashSet<String> inValue, String separator){
        String values = "";
        for(String value : inValue) {
            values += value + separator;
        }
        values = values.substring(0, values.length()-1);
        return values;
    }

    public static String arrayToString(String[] inValue, String separator){
        String values = "";
        for(String value : inValue) {
            values += value + separator;
        }
        values = values.substring(0, values.length()-1);
        return values;
    }
}
