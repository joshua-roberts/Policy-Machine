package gov.nist.policyserver.dao;

import gov.nist.policyserver.analytics.PmAnalytics;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.graph.PmGraph;

import java.io.IOException;

public interface GraphDAO {

    PmGraph buildGraph() throws DatabaseException;

    PmGraph getGraph();

    PmAnalytics getAnalytics();

    void reset() throws DatabaseException;
}
