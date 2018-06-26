package gov.nist.policyserver.dao.sql;

import gov.nist.policyserver.dao.NodesDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.nodes.Property;

public class SqlNodesDAO implements NodesDAO {
    @Override
    public Node createNode(long id, String name, NodeType nt) throws DatabaseException {
        return null;
    }

    @Override
    public void updateNode(long nodeId, String name) throws DatabaseException {

    }

    @Override
    public void deleteNode(long nodeId) throws DatabaseException {

    }

    @Override
    public void addNodeProperty(long nodeId, Property property) throws DatabaseException {

    }

    @Override
    public void deleteNodeProperty(long nodeId, String key) throws DatabaseException {

    }

    @Override
    public void updateNodeProperty(long nodeId, String key, String value) throws DatabaseException {

    }
}
