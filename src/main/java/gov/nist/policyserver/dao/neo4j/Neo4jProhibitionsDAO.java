package gov.nist.policyserver.dao.neo4j;

import gov.nist.policyserver.dao.ProhibitionsDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.model.prohibitions.ProhibitionResource;
import gov.nist.policyserver.model.prohibitions.ProhibitionSubject;
import gov.nist.policyserver.model.prohibitions.ProhibitionSubjectType;

import java.sql.Connection;
import java.util.HashSet;

import static gov.nist.policyserver.dao.neo4j.Neo4jHelper.*;

public class Neo4jProhibitionsDAO implements ProhibitionsDAO {

    private static String PROHIBITION_LABEL = "prohibition";

    private Connection connection;

    public Neo4jProhibitionsDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createProhibition(String prohibitionName, HashSet<String> operations, boolean intersection, ProhibitionResource[] resources, ProhibitionSubject subject) throws DatabaseException {
        String cypher = "create (:" + PROHIBITION_LABEL + "{" +
                "name: '" + prohibitionName + "', " +
                "operations: " + setToCypherArray(operations) +
                ", intersection: " + intersection +
                "})";
        execute(connection, cypher);

        for(ProhibitionResource pr : resources){
            addResourceToProhibition(prohibitionName, pr.getResourceId(), pr.isComplement());
        }

        setProhibitionSubject(prohibitionName, subject.getSubjectId(), subject.getSubjectType());
    }

    @Override
    public void deleteProhibition(String prohibitionName) throws DatabaseException {
        String cypher = "match(p:" + PROHIBITION_LABEL +") detach delete p";
        execute(connection, cypher);
    }

    @Override
    public void addResourceToProhibition(String prohibitionName, long resourceId, boolean complement) throws DatabaseException {
        String cypher = "match(p:" + PROHIBITION_LABEL + "{name:'" + prohibitionName + "'}), (n{id:" + resourceId +"}) create (p)-[:" + PROHIBITION_LABEL +"{complement: " + complement + "}]->(n)";
        execute(connection, cypher);
    }

    @Override
    public void deleteProhibitionResource(String prohibitionName, long resourceId) throws DatabaseException {
        String cypher = "match(n{id:" + resourceId + "})<-[r:" + PROHIBITION_LABEL +"]-(p:" + PROHIBITION_LABEL +"{name:'" + prohibitionName + "'}) delete r";
        execute(connection, cypher);
    }

    @Override
    public void setProhibitionIntersection(String prohibitionName, boolean intersection) throws DatabaseException {
        String cypher = "match(d:" + PROHIBITION_LABEL +"{name:'" + prohibitionName + "'}) set d.intersection = " + intersection;
        execute(connection, cypher);
    }

    @Override
    public void setProhibitionSubject(String prohibitionName, long subjectId, ProhibitionSubjectType subjectType) throws DatabaseException {
        String cypher;
        if(subjectType.equals(ProhibitionSubjectType.P)) {
            cypher = "match(p:" + PROHIBITION_LABEL + "{name:'" + prohibitionName + "'}) create (p)<-[:" + PROHIBITION_LABEL + "]-(:PP{subjectId:" + subjectId + ", subjectType:'" + subjectType + "'})";
        } else {
            cypher = "match(p:" + PROHIBITION_LABEL + "{name:'" + prohibitionName + "'}), (n{id:" + subjectId + ", type:'" + subjectType + "'}) create (p)<-[:" + PROHIBITION_LABEL + "]-(n)";
        }
        execute(connection, cypher);
    }

    @Override
    public void setProhibitionOperations(String prohibitionName, HashSet<String> operations) throws DatabaseException {
        String opStr = setToCypherArray(operations);
        String cypher = "match(p:" + PROHIBITION_LABEL +"{name:'" + prohibitionName + "'}) set p.operations = " + opStr;
        execute(connection, cypher);
    }
}
