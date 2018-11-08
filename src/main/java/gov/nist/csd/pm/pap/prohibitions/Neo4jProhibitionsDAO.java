package gov.nist.csd.pm.pap.prohibitions;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.prohibitions.*;
import gov.nist.csd.pm.pap.db.neo4j.Neo4jConnection;
import gov.nist.csd.pm.pap.db.neo4j.Neo4jHelper;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.loader.prohibitions.Neo4jProhibitionsLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_DB;
import static gov.nist.csd.pm.pap.db.neo4j.Neo4jHelper.setToCypherArray;

/**
 * Neo4j implementation of the ProhibitionsDAO interface.
 */
public class Neo4jProhibitionsDAO implements ProhibitionsDAO {

    private Neo4jConnection neo4j;
    private DatabaseContext ctx;

    /**
     * Create a new Neo4jProhibitionsDAO with the given database context information.
     * @param ctx The database connection information.
     * @throws DatabaseException If there is an error establishing a connection to the Neo4j instance.
     */
    public Neo4jProhibitionsDAO(DatabaseContext ctx) throws DatabaseException {
        this.ctx = ctx;
        this.neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
    }

    /**
     * Create a prohibition in the database.
     *
     * @param prohibition The prohibition to be created.
     * @throws DatabaseException If there is an error creating in the prohibition in the database.
     */
    @Override
    public void createProhibition(Prohibition prohibition) throws DatabaseException {
        String name = prohibition.getName();
        ProhibitionSubject subject = prohibition.getSubject();
        HashSet<String> operations = prohibition.getOperations();
        List<ProhibitionNode> nodes = prohibition.getNodes();
        boolean intersection = prohibition.isIntersection();

        String nodesStr = "";
        for (ProhibitionNode pr : nodes) {
            nodesStr += String.format("with p create(p)<-[:prohibition]-(pr:prohibition:%s{resourceID:%d, complement:%b})", name, pr.getID(), pr.isComplement());
        }

        String cypher =
                String.format("create (:prohibition:%s{name: '%s', operations: %s, intersection: %b}) ", name, name, setToCypherArray(operations), intersection) +
                        "with p" +
                        String.format("create(p)<-[:prohibition]-(ps:prohibition:%s{subjectID:%d, subjectType:'%s'})", name, subject.getSubjectID(), subject.getSubjectType()) +
                        nodesStr;

        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
        ) {
            stmt.executeQuery();
        }catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    /**
     * Get all prohibitions from the database. Pass the current database context information to a Prohibitions loader
     * to do the loading.
     *
     * @return A List of the prohibitions from the database.
     * @throws DatabaseException If there is an error retrieving the prohibitions from the database.
     * @throws InvalidProhibitionSubjectTypeException If there is a Prohibition with an invalid subject type.
     */
    @Override
    public List<Prohibition> getProhibitions() throws DatabaseException, InvalidProhibitionSubjectTypeException {
        return new Neo4jProhibitionsLoader(ctx).loadProhibitions();
    }

    @Override
    public Prohibition getProhibition(String prohibitionName) throws DatabaseException, InvalidProhibitionSubjectTypeException {
        Prohibition prohibition = null;

        String cypher = "match(p:prohibition{name: " + prohibitionName + "}) return p.name, p.operations, p.intersection";
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                String name = rs.getString(1);
                HashSet<String> ops = Neo4jHelper.getStringSetFromJson(rs.getString(2));
                boolean inter = rs.getBoolean(3);

                //get subject
                ProhibitionSubject subject = null;
                cypher = "match(d:prohibition{name:'" + name + "'})<-[:prohibition]-(s) return s.subjectID, s.subjectType";
                try(
                        Connection subjectConn = neo4j.getConnection();
                        PreparedStatement subjectStmt = subjectConn.prepareStatement(cypher);
                        ResultSet subjectRs = subjectStmt.executeQuery()
                ) {
                    if (subjectRs.next()) {
                        long subjectID = subjectRs.getLong(1);
                        String subjectType = subjectRs.getString(2);
                        subject = new ProhibitionSubject(subjectID, ProhibitionSubjectType.toType(subjectType));
                    }
                }

                //get nodes
                List<ProhibitionNode> nodes = new ArrayList<>();
                cypher = "match(d:prohibition{name:'" + name + "'})-[r:prohibition]->(s) return s.resourceID, r.complement";
                try(
                        Connection resConn = neo4j.getConnection();
                        PreparedStatement resStmt = resConn.prepareStatement(cypher);
                        ResultSet resRs = resStmt.executeQuery()
                ) {
                    while(resRs.next()) {
                        long resourceID = resRs.getLong(1);
                        boolean comp = resRs.getBoolean(2);
                        nodes.add(new ProhibitionNode(resourceID, comp));
                    }
                }

                prohibition = new Prohibition(name, subject, nodes, ops, inter);
            }
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }

        return prohibition;
    }

    @Override
    public void deleteProhibition(String prohibitionName) throws DatabaseException {
        String cypher = "match(p:prohibition:" + prohibitionName + "}) detach delete n";

        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
        ) {
            stmt.executeQuery();


        }catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public void updateProhibition(Prohibition prohibition) throws DatabaseException {
        deleteProhibition(prohibition.getName());
        createProhibition(prohibition);
    }
}
