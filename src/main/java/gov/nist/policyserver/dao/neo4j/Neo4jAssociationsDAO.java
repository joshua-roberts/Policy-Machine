package gov.nist.policyserver.dao.neo4j;

import gov.nist.policyserver.dao.AssociationsDAO;
import gov.nist.policyserver.exceptions.DatabaseException;

import java.sql.Connection;
import java.util.HashSet;

import static gov.nist.policyserver.dao.neo4j.Neo4jHelper.execute;
import static gov.nist.policyserver.dao.neo4j.Neo4jHelper.setToCypherArray;

public class Neo4jAssociationsDAO implements AssociationsDAO {

    private Connection connection;

    public Neo4jAssociationsDAO(Connection connection) {
        this.connection = connection;
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
