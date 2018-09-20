package gov.nist.csd.pm.pip.dao.neo4j;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.pip.dao.ProhibitionsDAO;
import gov.nist.csd.pm.model.prohibitions.ProhibitionResource;
import gov.nist.csd.pm.model.prohibitions.ProhibitionSubject;
import gov.nist.csd.pm.model.prohibitions.ProhibitionSubjectType;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.util.HashSet;

public class Neo4jProhibitionsDAO implements ProhibitionsDAO {

    private static String PROHIBITION_LABEL = "prohibition";

    private Neo4jConnection neo4j;

    public Neo4jProhibitionsDAO(DatabaseContext ctx) throws DatabaseException {
        neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
    }

    @Override
    public void createProhibition(String prohibitionName, HashSet<String> operations, boolean intersection, ProhibitionResource[] resources, ProhibitionSubject subject) throws DatabaseException, DatabaseException {
        String cypher = "create (:" + PROHIBITION_LABEL + "{" +
                "name: '" + prohibitionName + "', " +
                "operations: " + neo4j.setToCypherArray(operations) +
                ", intersection: " + intersection +
                "})";
        neo4j.execute(cypher);

        for(ProhibitionResource pr : resources){
            addResourceToProhibition(prohibitionName, pr.getResourceID(), pr.isComplement());
        }

        setProhibitionSubject(prohibitionName, subject.getSubjectID(), subject.getSubjectType());
    }

    @Override
    public void deleteProhibition(String prohibitionName) throws DatabaseException {
        String cypher = "match(p:" + PROHIBITION_LABEL +") detach delete p";
        neo4j.execute(cypher);
    }

    @Override
    public void addResourceToProhibition(String prohibitionName, long resourceID, boolean complement) throws DatabaseException {
        String cypher = "match(p:" + PROHIBITION_LABEL + "{name:'" + prohibitionName + "'}), (n{id:" + resourceID +"}) create (p)-[:" + PROHIBITION_LABEL +"{complement: " + complement + "}]->(n)";
        neo4j.execute(cypher);
    }

    @Override
    public void deleteProhibitionResource(String prohibitionName, long resourceID) throws DatabaseException {
        String cypher = "match(n{id:" + resourceID + "})<-[r:" + PROHIBITION_LABEL +"]-(p:" + PROHIBITION_LABEL +"{name:'" + prohibitionName + "'}) delete r";
        neo4j.execute(cypher);
    }

    @Override
    public void setProhibitionIntersection(String prohibitionName, boolean intersection) throws DatabaseException {
        String cypher = "match(d:" + PROHIBITION_LABEL +"{name:'" + prohibitionName + "'}) set d.intersection = " + intersection;
        neo4j.execute(cypher);
    }

    @Override
    public void setProhibitionSubject(String prohibitionName, long subjectID, ProhibitionSubjectType subjectType) throws DatabaseException {
        String cypher;
        if(subjectType.equals(ProhibitionSubjectType.P)) {
            cypher = "match(p:" + PROHIBITION_LABEL + "{name:'" + prohibitionName + "'}) create (p)<-[:" + PROHIBITION_LABEL + "]-(:process{id:" + subjectID + ", type:'" + subjectType + "'})";
        } else {
            cypher = "match(p:" + PROHIBITION_LABEL + "{name:'" + prohibitionName + "'}), (n{id:" + subjectID + ", type:'" + subjectType + "'}) create (p)<-[:" + PROHIBITION_LABEL + "]-(n)";
        }
        neo4j.execute(cypher);
    }

    @Override
    public void setProhibitionOperations(String prohibitionName, HashSet<String> operations) throws DatabaseException {
        String opStr = neo4j.setToCypherArray(operations);
        String cypher = "match(p:" + PROHIBITION_LABEL +"{name:'" + prohibitionName + "'}) set p.operations = " + opStr;
        neo4j.execute(cypher);
    }
}
