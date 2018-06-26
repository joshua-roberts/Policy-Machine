package gov.nist.policyserver.dao.neo4j;

import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.helpers.JsonHelper;
import gov.nist.policyserver.model.graph.nodes.Node;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static gov.nist.policyserver.common.Constants.ERR_NEO;

public class Neo4jHelper {

    public static ResultSet execute(Connection conn, String cypher) throws DatabaseException {
        try {
            PreparedStatement stmt = conn.prepareStatement(cypher);
            return stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    public static List<Node> getNodesFromResultSet(ResultSet rs) throws DatabaseException {
        List<Node> nodes = new ArrayList<>();

        try {
            while (rs.next()) {
                Node node = JsonHelper.getNodeFromJson(rs.getString(1));
                nodes.add(node);
            }
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }

        return nodes;
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
}
