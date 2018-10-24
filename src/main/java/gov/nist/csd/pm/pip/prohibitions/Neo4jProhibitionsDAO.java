package gov.nist.csd.pm.pip.prohibitions;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.pip.db.neo4j.Neo4jConnection;
import gov.nist.csd.pm.pip.db.neo4j.Neo4jHelper;
import gov.nist.csd.pm.model.prohibitions.ProhibitionResource;
import gov.nist.csd.pm.model.prohibitions.ProhibitionSubject;
import gov.nist.csd.pm.model.prohibitions.ProhibitionSubjectType;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

import static gov.nist.csd.pm.pep.response.ErrorCodes.ERR_NEO;

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
                "operations: " + Neo4jHelper.setToCypherArray(operations) +
                ", intersection: " + intersection +
                "})";
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }

        for(ProhibitionResource pr : resources){
            addResourceToProhibition(prohibitionName, pr.getResourceID(), pr.isComplement());
        }

        setProhibitionSubject(prohibitionName, subject.getSubjectID(), subject.getSubjectType());
    }

    @Override
    public void deleteProhibition(String prohibitionName) throws DatabaseException {
        String cypher = "match(p:" + PROHIBITION_LABEL +") detach delete p";
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    @Override
    public void addResourceToProhibition(String prohibitionName, long resourceID, boolean complement) throws DatabaseException {
        String cypher = "match(p:" + PROHIBITION_LABEL + "{name:'" + prohibitionName + "'}), (n{id:" + resourceID +"}) create (p)-[:" + PROHIBITION_LABEL +"{complement: " + complement + "}]->(n)";
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }    }

    @Override
    public void deleteProhibitionResource(String prohibitionName, long resourceID) throws DatabaseException {
        String cypher = "match(n{id:" + resourceID + "})<-[r:" + PROHIBITION_LABEL +"]-(p:" + PROHIBITION_LABEL +"{name:'" + prohibitionName + "'}) delete r";
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }    }

    @Override
    public void setProhibitionIntersection(String prohibitionName, boolean intersection) throws DatabaseException {
        String cypher = "match(d:" + PROHIBITION_LABEL +"{name:'" + prohibitionName + "'}) set d.intersection = " + intersection;
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }    }

    @Override
    public void setProhibitionSubject(String prohibitionName, long subjectID, ProhibitionSubjectType subjectType) throws DatabaseException {
        String cypher;
        if(subjectType.equals(ProhibitionSubjectType.P)) {
            cypher = "match(p:" + PROHIBITION_LABEL + "{name:'" + prohibitionName + "'}) create (p)<-[:" + PROHIBITION_LABEL + "]-(:process{id:" + subjectID + ", type:'" + subjectType + "'})";
        } else {
            cypher = "match(p:" + PROHIBITION_LABEL + "{name:'" + prohibitionName + "'}), (n{id:" + subjectID + ", type:'" + subjectType + "'}) create (p)<-[:" + PROHIBITION_LABEL + "]-(n)";
        }
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }    }

    @Override
    public void setProhibitionOperations(String prohibitionName, HashSet<String> operations) throws DatabaseException {
        String opStr = Neo4jHelper.setToCypherArray(operations);
        String cypher = "match(p:" + PROHIBITION_LABEL +"{name:'" + prohibitionName + "'}) set p.operations = " + opStr;
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }    }
}
