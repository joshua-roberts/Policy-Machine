package gov.nist.csd.pm.pap.loader.prohibitions;

import gov.nist.csd.pm.common.exceptions.DatabaseException;
import gov.nist.csd.pm.common.exceptions.InvalidProhibitionSubjectTypeException;
import gov.nist.csd.pm.common.model.prohibitions.Prohibition;
import gov.nist.csd.pm.common.model.prohibitions.ProhibitionNode;
import gov.nist.csd.pm.common.model.prohibitions.ProhibitionSubject;
import gov.nist.csd.pm.common.model.prohibitions.ProhibitionSubjectType;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.db.neo4j.Neo4jConnection;
import gov.nist.csd.pm.pap.db.neo4j.Neo4jHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static gov.nist.csd.pm.common.exceptions.ErrorCodes.ERR_DB;

/**
 * Neo4j implementation of the ProhibitionsLoader interface. Load prohibitions from a Neo4j instance into memory.
 */
public class Neo4jProhibitionsLoader implements ProhibitionsLoader {

    /**
     * Object to hold connection to Neo4j instance.
     */
    private Neo4jConnection neo4j;

    /**
     * Create a new ProhibitionsLoader from Neo4j, using the provided database connection parameters.
     * @param ctx The parameters to connect to the database
     * @throws DatabaseException If a connection cannot be made to the database
     */
    public Neo4jProhibitionsLoader(DatabaseContext ctx) throws DatabaseException {
        neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
    }

    /**
     * Load all of the  prohibitions in the database into a memory structure.
     * @return The list of all prohibitions.
     * @throws DatabaseException If there is an error getting a prohibition form the database.
     * @throws InvalidProhibitionSubjectTypeException If a prohibition in the database has an invalid subject type.
     */
    @Override
    public List<Prohibition> loadProhibitions() throws DatabaseException, InvalidProhibitionSubjectTypeException {
        List<Prohibition> prohibitions = new ArrayList<>();

        String cypher = "match(p:prohibition) return p.name, p.operations, p.intersection";
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
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

                prohibitions.add(new Prohibition(name, subject, nodes, ops, inter));
            }
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }

        return prohibitions;
    }
}
