package gov.nist.csd.pm.pip.neo4j;

import com.google.gson.Gson;
import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.graph.Node;

import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;

import static gov.nist.policyserver.common.Constants.ERR_NEO;

public class Neo4jDatabase {
    private String host;
    private int port;
    private String username;
    private String password;
    private Connection connection;

    public Neo4jDatabase(String host, int port, String username, String password) throws DatabaseException {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;

        try {
            Driver driver = new org.neo4j.jdbc.Driver();
            DriverManager.registerDriver(driver);
            connection = DriverManager.getConnection("jdbc:neo4j:http://" + host + ":" + port + "", username, password);
        } catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    public ResultSet execute(String cypher) throws DatabaseException {
        try {
            PreparedStatement stmt = connection.prepareStatement(cypher);
            return stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    public static HashSet<Node> getNodesFromResultSet(ResultSet rs) throws DatabaseException {
        HashSet<Node> nodes = new HashSet<>();

        try {
            while (rs.next()) {
                Node node = getNode(rs.getString(1));
                nodes.add(node);
            }
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }

        return nodes;
    }

    protected static Node getNode(String json) {
        return new Gson().fromJson(json, Node.class);
    }

    protected static String setToCypherArray(HashSet<String> set) {
        String str = "[";
        for (String op : set) {
            op = "'" + op + "'";
            if (str.length()==1) {
                str += op;
            }
            else {
                str += "," + op;
            }
        }
        str += "]";
        return str;
    }

    protected static String toCypherArray(String[] ops) {
        String str = "[";
        for (String op : ops) {
            op = "'" + op + "'";
            if (str.length()==1) {
                str += op;
            }
            else {
                str += "," + op;
            }
        }
        str += "]";
        return str;
    }

    protected static HashSet<String> getStringSetFromJson(String json) {
        HashSet<String> ops = new HashSet<>();
        String[] opsArr = json.replaceAll("[\\[\\]\"]", "").split(",\\s*");
        if(!opsArr[0].isEmpty()){
            ops.addAll(Arrays.asList(opsArr));
        }
        return ops;
    }
}
