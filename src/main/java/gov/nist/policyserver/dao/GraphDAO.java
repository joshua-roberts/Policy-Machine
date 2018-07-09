package gov.nist.policyserver.dao;

import gov.nist.policyserver.analytics.PmAnalytics;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.graph.PmGraph;

import java.io.IOException;
import java.sql.SQLException;

public interface GraphDAO {

    PmGraph buildGraph() throws DatabaseException, SQLException, IOException, ClassNotFoundException;

    PmGraph getGraph();

    PmAnalytics getAnalytics();

    void reset() throws DatabaseException;
}
