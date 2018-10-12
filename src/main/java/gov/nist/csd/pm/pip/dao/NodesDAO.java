package gov.nist.csd.pm.pip.dao;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.graph.NodeType;

import java.util.Map;

public interface NodesDAO {
    Node createNode(long id, String name, NodeType nt, Map<String, String> properties) throws DatabaseException;

    void updateNode(long nodeId, String name) throws DatabaseException;

    void deleteNode(long nodeId) throws DatabaseException;

    void addNodeProperty(long nodeId, String key, String value) throws DatabaseException;

    void deleteNodeProperty(long nodeId, String key) throws DatabaseException;

    void updateNodeProperty(long nodeId, String key, String value) throws DatabaseException;
}
