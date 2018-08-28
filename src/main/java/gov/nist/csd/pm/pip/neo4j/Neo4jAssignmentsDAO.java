package gov.nist.csd.pm.pip.neo4j;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.pip.AssignmentsDAO;
import gov.nist.csd.pm.model.graph.Node;

import java.sql.Connection;

import static gov.nist.csd.pm.pip.neo4j.Neo4jHelper.execute;

public class Neo4jAssignmentsDAO implements AssignmentsDAO {

    private Connection connection;

    public Neo4jAssignmentsDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createAssignment(Node child, Node parent) throws DatabaseException {
        String cypher = "MATCH (a:" + child.getType() + " {id:" + child.getId() + "}), (b:" + parent.getType() + " {id:" + parent.getId() + "}) " +
                "CREATE (a)-[:assigned_to]->(b)";
        execute(connection, cypher);
    }

    @Override
    public void deleteAssignment(long childId, long parentId) throws DatabaseException {
        String cypher = "match (a{id:" + childId + "})-[r:assigned_to]->(b{id:" + parentId + "}) delete r";
        execute(connection, cypher);
    }
}
