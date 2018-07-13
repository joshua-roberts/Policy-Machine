package gov.nist.policyserver.dao;

import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.helpers.JsonHelper;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.nodes.Property;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static gov.nist.policyserver.common.Constants.ERR_NEO;
import static gov.nist.policyserver.dao.neo4j.Neo4jHelper.execute;
import static gov.nist.policyserver.dao.neo4j.Neo4jHelper.getNodesFromResultSet;

public interface NodesDAO {
    Node createNode(long id, String name, NodeType nt) throws DatabaseException;

    void updateNode(long nodeId, String name) throws DatabaseException;

    void deleteNode(long nodeId) throws DatabaseException;

    void addNodeProperty(long nodeId, Property property) throws DatabaseException;

    void deleteNodeProperty(long nodeId, String key) throws DatabaseException;

    void updateNodeProperty(long nodeId, String key, String value) throws DatabaseException;

}
