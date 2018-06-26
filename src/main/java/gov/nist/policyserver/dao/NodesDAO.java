package gov.nist.policyserver.dao;

import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.nodes.Property;

public interface NodesDAO {
    Node createNode(long id, String name, NodeType nt) throws DatabaseException;

    void updateNode(long nodeId, String name) throws DatabaseException;

    void deleteNode(long nodeId) throws DatabaseException;

    void addNodeProperty(long nodeId, Property property) throws DatabaseException;

    void deleteNodeProperty(long nodeId, String key) throws DatabaseException;

    void updateNodeProperty(long nodeId, String key, String value) throws DatabaseException;
}
