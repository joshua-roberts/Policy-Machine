package gov.nist.policyserver.dao.neo4j;

import gov.nist.policyserver.dao.AssignmentsDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.helpers.JsonHelper;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.relationships.Assignment;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static gov.nist.policyserver.common.Constants.ERR_NEO;
import static gov.nist.policyserver.dao.neo4j.Neo4jHelper.execute;

public class Neo4jAssignmentsDAO implements AssignmentsDAO {

    private Connection connection;

    public Neo4jAssignmentsDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createAssignment(long childId, long parentId) throws DatabaseException {
        String cypher = "MATCH (a {id:" + childId + "}), (b {id:" + parentId + "}) " +
                "CREATE (a)-[:assigned_to]->(b)";
        execute(connection, cypher);
    }

    @Override
    public void deleteAssignment(long childId, long parentId) throws DatabaseException {
        String cypher = "match (a{id:" + childId + "})-[r:assigned_to]->(b{id:" + parentId + "}) delete r";
        execute(connection, cypher);
    }
}
