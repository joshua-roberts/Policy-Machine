package gov.nist.policyserver.service;

import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.nodes.Property;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static gov.nist.policyserver.common.Constants.*;

public class NodeService extends Service{

    public HashSet<Node> getNodes(String namespace, String name, String type, String key, String value)
            throws InvalidNodeTypeException, InvalidPropertyException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        NodeType nodeType = (type != null) ? NodeType.toNodeType(type) : null;
        Property property = (key==null||value==null)?null : new Property(key, value);

        HashSet<Node> nodes = getGraph().getNodes();

        //check namespace match
        if(namespace != null){
            nodes.removeIf(node -> {
                try {
                    return !node.hasProperty(NAMESPACE_PROPERTY) || !node.getProperty(NAMESPACE_PROPERTY).getValue().equalsIgnoreCase(namespace);
                }
                catch (PropertyNotFoundException e) {
                    return true;
                }
            });
        }

        //check name match
        if(name != null){
            nodes.removeIf(node -> !node.getName().equals(name));
        }

        //check type match
        if(nodeType != null){
            nodes.removeIf(node -> !node.getType().equals(nodeType));
        }

        //check property match
        if(property != null) {
            nodes.removeIf(node -> !node.hasProperty(property));
        }

        return nodes;
    }

    public HashSet<Node> getNodes(String namespace, String name, String type, List<Property> properties)
            throws InvalidNodeTypeException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        NodeType nodeType = (type != null) ? NodeType.toNodeType(type) : null;

        HashSet<Node> nodes = getGraph().getNodes();

        //check namespace match
        if(namespace != null){
            nodes.removeIf(node -> {
                try {
                    return !node.hasProperty(NAMESPACE_PROPERTY) || !node.getProperty(NAMESPACE_PROPERTY).getValue().equalsIgnoreCase(namespace);
                }
                catch (PropertyNotFoundException e) {
                    return true;
                }
            });
        }

        //check name match
        if(name != null){
            nodes.removeIf(node -> !node.getName().equals(name));
        }

        //check type match
        if(nodeType != null){
            nodes.removeIf(node -> !node.getType().equals(nodeType));
        }

        //check property match
        if(properties != null && !properties.isEmpty()) {
            nodes.removeIf(node -> {
                for (Property prop : properties) {
                    if(node.hasProperty(prop)) {
                        return false;
                    }
                }
                return true;
            });
        }

        return nodes;
    }

    public Node getNode(String namespace, String name, String type, List<Property> properties)
            throws InvalidNodeTypeException, UnexpectedNumberOfNodesException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        NodeType nodeType = (type != null) ? NodeType.toNodeType(type) : null;

        HashSet<Node> nodes = getGraph().getNodes();

        //check namespace match
        if(namespace != null){
            nodes.removeIf(node -> {
                try {
                    return !node.hasProperty(NAMESPACE_PROPERTY) || !node.getProperty(NAMESPACE_PROPERTY).getValue().equalsIgnoreCase(namespace);
                }
                catch (PropertyNotFoundException e) {
                    return true;
                }
            });
        }

        //check name match
        if(name != null){
            nodes.removeIf(node -> !node.getName().equals(name));
        }

        //check type match
        if(nodeType != null){
            nodes.removeIf(node -> !node.getType().equals(nodeType));
        }

        //check property match
        if(properties != null && !properties.isEmpty()) {
            nodes.removeIf(node -> {
                for (Property prop : properties) {
                    if(node.hasProperty(prop)) {
                        return false;
                    }
                }
                return true;
            });
        }

        if(nodes.size() != 1) {
            throw new UnexpectedNumberOfNodesException();
        }

        return nodes.iterator().next();
    }

    public HashSet<Node> getNodes(HashSet<Node> nodes, String namespace, String name, String type, String key, String value)
            throws InvalidNodeTypeException, InvalidPropertyException {
        NodeType nodeType = (type != null) ? NodeType.toNodeType(type) : null;
        Property property = (key==null||value==null)?null : new Property(key, value);

        //check namespace match
        if(namespace != null){
            nodes.removeIf(node -> {
                try {
                    return !node.hasProperty(NAMESPACE_PROPERTY) || !node.getProperty(NAMESPACE_PROPERTY).getValue().equalsIgnoreCase(namespace);
                }
                catch (PropertyNotFoundException e) {
                    return true;
                }
            });
        }

        //check name match
        if(name != null){
            nodes.removeIf(node -> !node.getName().equals(name));
        }

        //check type match
        if(nodeType != null){
            nodes.removeIf(node -> !node.getType().equals(nodeType));
        }

        //check property match
        if(property != null) {
            nodes.removeIf(node -> !node.hasProperty(property));
        }

        return nodes;
    }

    public Node getNode(String name, String type, String properties) throws InvalidPropertyException, InvalidNodeTypeException, UnexpectedNumberOfNodesException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        //get target node
        //get properties
        List<Property> propList = new ArrayList<>();
        if(properties != null) {
            String[] propertiesArr = properties.split(",\\s*");
            for (String prop : propertiesArr) {
                String[] split = prop.split("=");
                if (split.length == 2) {
                    propList.add(new Property(split[0], split[1]));
                }
            }
        }
        return getNode(null, name, type, propList);
    }

    public Node createNode(long baseId, long id, String name, String type, Property[] properties)
            throws NullNameException, NullTypeException, InvalidNodeTypeException,
            InvalidPropertyException, NodeNameExistsInNamespaceException, DatabaseException,
            ConfigurationException, NodeNameExistsException, NodeIdExistsException,
            NodeNotFoundException, InvalidAssignmentException, AssignmentExistsException, IOException, ClassNotFoundException, SQLException {
        //check name and type are not null
        if(name == null){
            throw new NullNameException();
        }
        if(type == null){
            throw new NullTypeException();
        }

        if(id != 0) {
            //check if ID exists
            try {
                Node node = getNode(id);
                throw new NodeIdExistsException(id, node);
            }
            catch (NodeNotFoundException e) {/*expected exception*/}
        }

        boolean checkDefault = true;
        //check this name will be the only one in the namespace
        if(properties != null) {
            for (Property property : properties) {
                //check if namespace property exists
                if (property.isValid() && property.getKey().equals(NAMESPACE_PROPERTY)) {
                    HashSet<Node> nodes = getNodes(property.getValue(), name, null, null, null);

                    if (nodes.size() > 0) {
                        throw new NodeNameExistsInNamespaceException(property.getValue(), name);
                    }

                    checkDefault = false;
                    break;
                }
            }
        }

        if(checkDefault){
            //check if name exists in the default namespace
            HashSet<Node> nodes = getNodes(null, name, type, null, null);
            if (!nodes.isEmpty()) {
                throw new NodeNameExistsException(name);
            }
        }

        //create node in database
        NodeType nt = NodeType.toNodeType(type);
        Node newNode = getDaoManager().getNodesDAO().createNode(id, name, nt);

        //add the node to the nodes
        getGraph().addNode(newNode);

        AssignmentService assignmentService = new AssignmentService();
        if (newNode.getId() > 0) {
            if(baseId > 0) {
                assignmentService.createAssignment(newNode.getId(), baseId);
            }
            //assign node to connector
            assignmentService.createAssignment(newNode.getId(), getConnector().getId());
        }

        //add properties to the node
        try {
            newNode = addNodeProperties(newNode, properties);
        }
        catch (PropertyNotFoundException e) {
            e.printStackTrace();
        }

        return newNode;
    }

    private Node addNodeProperties(Node node, Property[] properties) throws NodeNotFoundException, DatabaseException, ConfigurationException, InvalidPropertyException, PropertyNotFoundException, SQLException, IOException, ClassNotFoundException {
        if(properties != null) {
            for (Property property : properties) {
                if(property.isValid()) {
                    try {
                        if(node.hasProperty(property.getKey())) {
                            updateNodeProperty(node.getId(), property.getKey(), property.getValue());
                        } else {
                            if(property.getKey().equals(PASSWORD_PROPERTY)) {
                                //check ic password is already hashed, and hash it if not
                                //this will occur when loading a configuration
                                if(property.getValue().length() != HASH_LENGTH) {
                                    String hash = generatePasswordHash(property.getValue());
                                    property.setValue(hash);
                                }
                            }
                            addNodeProperty(node.getId(), property.getKey(), property.getValue());
                        }
                    }
                    catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                        throw new InvalidPropertyException("Could not add password property. Node was created anyways.");
                    }
                }
            }
        }

        return node;
    }

    public Node getNode(long nodeId)
            throws NodeNotFoundException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        Node node = getGraph().getNode(nodeId);
        if(node == null){
            throw new NodeNotFoundException(nodeId);
        }

        return node;
    }

    public Node getNodeInNamespace(String namespace, String name)
            throws NameInNamespaceNotFoundException, InvalidNodeTypeException, InvalidPropertyException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        HashSet<Node> nodes = getNodes(namespace, name, null, null, null);
        if(nodes.isEmpty()){
            throw new NameInNamespaceNotFoundException(namespace, name);
        }

        return nodes.iterator().next();
    }

    public void deleteNodeInNamespace(String namespace, String nodeName)
            throws InvalidNodeTypeException, NameInNamespaceNotFoundException, InvalidPropertyException, NodeNotFoundException, DatabaseException, ConfigurationException, SQLException, IOException, ClassNotFoundException {
        //get the node in namespace
        Node node = getNodeInNamespace(namespace, nodeName);

        deleteNode(node.getId());
    }

    public Node updateNode(long nodeId, String name, Property[] properties) throws NodeNotFoundException, DatabaseException, ConfigurationException, InvalidPropertyException, PropertyNotFoundException, SQLException, IOException, ClassNotFoundException {
        //check node exists
        Node node = getNode(nodeId);

        //update node in the database
        getDaoManager().getNodesDAO().updateNode(nodeId, name);

        //update node in graph
        getGraph().updateNode(nodeId, name);

        //delete node properties
        deleteNodeProperties(nodeId);

        //add the new properties
        addNodeProperties(node, properties);

        return getGraph().getNode(nodeId);
    }

    public void deleteNode(long nodeId) throws NodeNotFoundException, DatabaseException, SQLException, IOException, ClassNotFoundException {
        //check node exists
        getNode(nodeId);

        //delete node in db
        getDaoManager().getNodesDAO().deleteNode(nodeId);

        //delete node in database
        getGraph().deleteNode(nodeId);
    }

    public List<Property> getNodeProperties(long nodeId) throws NodeNotFoundException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        //get node
        Node node = getNode(nodeId);

        return node.getProperties();
    }

    public Node addNodeProperty(long nodeId, String key, String value) throws InvalidPropertyException, NodeNotFoundException, DatabaseException, ConfigurationException, SQLException, IOException, ClassNotFoundException {
        Property prop = new Property(key, value);

        //check node exists
        Node node = getNode(nodeId);

        //add property to node in database
        getDaoManager().getNodesDAO().addNodeProperty(nodeId, prop);

        //add property to node in nodes
        node.addProperty(prop);

        return node;
    }

    public Property getNodeProperty(long nodeId, String key) throws NodeNotFoundException, PropertyNotFoundException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        //get node
        Node node = getNode(nodeId);

        //get node property
        return node.getProperty(key);
    }

    public void deleteNodeProperty(long nodeId, String key) throws NodeNotFoundException, PropertyNotFoundException, DatabaseException, ConfigurationException, SQLException, IOException, ClassNotFoundException {
        //check if the property exists
        getNodeProperty(nodeId, key);

        //delete the node property
        getDaoManager().getNodesDAO().deleteNodeProperty(nodeId, key);

        //delete node from the nodes
        getGraph().deleteNodeProperty(nodeId, key);
    }

    private void deleteNodeProperties(long nodeId) throws NodeNotFoundException, ConfigurationException, DatabaseException, SQLException, IOException, ClassNotFoundException {
        List<Property> props = getNodeProperties(nodeId);

        for(Property property : props) {
            getDaoManager().getNodesDAO().deleteNodeProperty(nodeId, property.getKey());
        }

        getGraph().deleteNodeProperties(nodeId);
    }

    public void updateNodeProperty(long nodeId, String key, String value) throws NodeNotFoundException, PropertyNotFoundException, ConfigurationException, DatabaseException, InvalidKeySpecException, NoSuchAlgorithmException, SQLException, IOException, ClassNotFoundException {
        //check if the property exists
        getNodeProperty(nodeId, key);

        if(key.equals(PASSWORD_PROPERTY)) {
            value = generatePasswordHash(value);
        }

        //update the property
        getDaoManager().getNodesDAO().updateNodeProperty(nodeId, key, value);

        //update property in graph
        getGraph().updateNodeProperty(nodeId, key, value);
    }

    public HashSet<Node> getChildrenOfType(long nodeId, String childType) throws NodeNotFoundException, InvalidNodeTypeException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        Node node = getNode(nodeId);

        HashSet<Node> children = getGraph().getChildren(node);
        HashSet<Node> retChildren = new HashSet<>();
        retChildren.addAll(children);
        if(childType != null) {
            NodeType nt = NodeType.toNodeType(childType);
            for (Node n : children) {
                if (!n.getType().equals(nt)) {
                    retChildren.remove(n);
                }
            }
        }
        return retChildren;
    }

    public void deleteNodeChildren(long nodeId, String childType) throws NodeNotFoundException, InvalidNodeTypeException, DatabaseException, ConfigurationException, SQLException, IOException, ClassNotFoundException {
        HashSet<Node> children = getChildrenOfType(nodeId, childType);
        for(Node node : children){
            //delete node in db
            getDaoManager().getNodesDAO().deleteNode(node.getId());

            //delete node in database
            getGraph().deleteNode(node.getId());
        }
    }

    public HashSet<Node> getParentsOfType(long nodeId, String parentType) throws InvalidNodeTypeException, NodeNotFoundException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        Node node = getNode(nodeId);

        HashSet<Node> parents = getGraph().getParents(node);
        HashSet<Node> retParents = new HashSet<>();
        retParents.addAll(parents);
        if(parentType != null) {
            NodeType nt = NodeType.toNodeType(parentType);
            for (Node n : parents) {
                if (!n.getType().equals(nt)) {
                    retParents.remove(n);
                }
            }
        }
        return retParents;
    }
}
