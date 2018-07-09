package gov.nist.policyserver.dao.neo4j;

import gov.nist.policyserver.dao.AssociationsDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.helpers.JsonHelper;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.relationships.Association;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static gov.nist.policyserver.common.Constants.ERR_NEO;
import static gov.nist.policyserver.dao.neo4j.Neo4jHelper.execute;
import static gov.nist.policyserver.dao.neo4j.Neo4jHelper.setToCypherArray;

public class Neo4jAssociationsDAO implements AssociationsDAO {

    private Connection connection;

    public Neo4jAssociationsDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Association> getAssociations() throws DatabaseException {
        List<Association> associations = new ArrayList<>();

        String cypher = "match(ua:UA)-[a:association]->(oa:OA) return ua,oa,a.operations,a.inherit;";
        ResultSet rs = execute(connection, cypher);
        try {
            while (rs.next()) {
                Node startNode = JsonHelper.getNodeFromJson(rs.getString(1));
                Node endNode = JsonHelper.getNodeFromJson(rs.getString(2));
                HashSet<String> ops = JsonHelper.getStringSetFromJson(rs.getString(3));
                boolean inherit = Boolean.valueOf(rs.getString(4));
                Association assoc = new Association(startNode, endNode, ops, inherit);
                associations.add(assoc);
            }
            return associations;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }
    @Override
    public void createAssociation(long uaId, long targetId, HashSet<String> operations, boolean inherit) throws DatabaseException {
        String ops = setToCypherArray(operations);
        String cypher = "MATCH (ua:UA{id:" + uaId + "}), (oa:OA {id:" + targetId + "}) " +
                "CREATE (ua)-[:association{label:'ar', inherit:'" + inherit + "', operations:" + ops + "}]->(oa)";
        execute(connection, cypher);
    }

    @Override
    public void updateAssociation(long uaId, long targetId, boolean inherit, HashSet<String> ops) throws DatabaseException {
        String strOps = setToCypherArray(ops);
        String cypher = "MATCH (ua:UA {id:" + uaId + "})-[r:association]->(oa:OA{id:" + targetId + "}) " +
                "SET r.operations=" + strOps;
        execute(connection, cypher);
    }

    @Override
    public void deleteAssociation(long uaId, long targetId) throws DatabaseException {
        String cypher = "match (a{id:" + uaId + "})-[r:association]->(b{id:" + targetId + "}) delete r";
        execute(connection, cypher);
    }
}
