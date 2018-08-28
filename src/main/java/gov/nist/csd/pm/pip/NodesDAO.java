package gov.nist.csd.pm.pip;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidNodeTypeException;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.graph.NodeType;

import java.util.HashMap;

public interface NodesDAO {
    Node createNode(long id, String name, NodeType nt) throws DatabaseException;

    void updateNode(long nodeId, String name) throws DatabaseException;

    void deleteNode(long nodeId) throws DatabaseException;

    void addNodeProperty(long nodeId, String key, String value) throws DatabaseException;

    void deleteNodeProperty(long nodeId, String key) throws DatabaseException;

    void updateNodeProperty(long nodeId, String key, String value) throws DatabaseException;

    void setNodeProperties(long nodeId, HashMap<String, String> properties) throws DatabaseException;

    Node createNode(long id, String name, String type, HashMap<String, String> properties) throws DatabaseException, InvalidNodeTypeException;
}
