package gov.nist.csd.pm.pap.loader.prohibitions;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.prohibitions.Prohibition;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.db.sql.SQLConnection;

import java.util.List;

public class SQLProhibitionsLoader implements ProhibitionsLoader {

    /**
     * Object to hold connection to Neo4j instance.
     */
    protected SQLConnection sql;

    /**
     * Create a new ProhibitionsLoader from SQL, using the provided database connection parameters.
     * @param ctx The parameters to connect to the database
     * @throws DatabaseException If a connection cannot be made to the database
     */
    public SQLProhibitionsLoader(DatabaseContext ctx) throws DatabaseException {
        sql = new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword(), ctx.getSchema());
    }

    @Override
    public List<Prohibition> loadProhibitions() {
        return null;
    }
}
