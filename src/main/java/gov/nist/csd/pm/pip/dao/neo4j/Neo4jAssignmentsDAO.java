package gov.nist.csd.pm.pip.dao.neo4j;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.pip.dao.AssignmentsDAO;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.pip.model.DatabaseContext;

public class Neo4jAssignmentsDAO implements AssignmentsDAO {

    private Neo4jConnection neo4j;

    public Neo4jAssignmentsDAO(DatabaseContext ctx) throws DatabaseException {
        neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
    }

    @Override
    public void createAssignment(Node child, Node parent) throws DatabaseException {
        String cypher = "MATCH (a:" + child.getType() + " {id:" + child.getID() + "}), (b:" + parent.getType() + " {id:" + parent.getID() + "}) " +
                "CREATE (a)-[:assigned_to]->(b)";
        neo4j.execute(cypher);
    }

    @Override
    public void deleteAssignment(long childID, long parentID) throws DatabaseException {
        String cypher = "match (a{id:" + childID + "})-[r:assigned_to]->(b{id:" + parentID + "}) delete r";
        neo4j.execute(cypher);
    }
}
