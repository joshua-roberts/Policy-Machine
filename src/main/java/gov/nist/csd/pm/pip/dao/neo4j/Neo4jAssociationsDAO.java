package gov.nist.csd.pm.pip.dao.neo4j;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.pip.dao.AssociationsDAO;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.util.HashSet;


public class Neo4jAssociationsDAO implements AssociationsDAO {

    private Neo4jConnection neo4j;

    public Neo4jAssociationsDAO(DatabaseContext ctx) throws DatabaseException {
        neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
    }

    @Override
    public void createAssociation(long uaID, long targetID, HashSet<String> operations) throws DatabaseException {
        String ops = neo4j.setToCypherArray(operations);
        String cypher = "MATCH (ua:UA{id:" + uaID + "}), (oa {id:" + targetID + "}) " +
                "CREATE (ua)-[:association{operations:" + ops + "}]->(target)";
        neo4j.execute(cypher);
    }

    @Override
    public void updateAssociation(long uaID, long targetID, HashSet<String> ops) throws DatabaseException {
        String strOps = neo4j.setToCypherArray(ops);
        String cypher = "MATCH (ua:UA {id:" + uaID + "})-[r:association]->(oa {id:" + targetID + "}) " +
                "SET r.operations=" + strOps;
        neo4j.execute(cypher);
    }

    @Override
    public void deleteAssociation(long uaId, long targetId) throws DatabaseException {
        String cypher = "match (a{id:" + uaId + "})-[r:association]->(b{id:" + targetId + "}) delete r";
        neo4j.execute(cypher);
    }
}
