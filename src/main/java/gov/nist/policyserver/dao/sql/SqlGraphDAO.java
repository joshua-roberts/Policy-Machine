package gov.nist.policyserver.dao.sql;

import gov.nist.policyserver.analytics.PmAnalytics;
import gov.nist.policyserver.dao.GraphDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.graph.PmGraph;

public class SqlGraphDAO implements GraphDAO {

    private PmGraph graph;

    public PmGraph getGraph() {
        return graph;
    }

    @Override
    public PmAnalytics getAnalytics() {
        return null;
    }

    @Override
    public PmGraph buildGraph() {
        return null;
    }

    @Override
    public void reset() throws DatabaseException {

    }
}
