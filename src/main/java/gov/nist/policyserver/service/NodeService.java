package gov.nist.policyserver.service;

import gov.nist.policyserver.analytics.PmAnalyticsEntry;
import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.helpers.ContentHelper;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.nodes.Property;
import gov.nist.policyserver.model.imports.ImportFile;
import gov.nist.policyserver.obligations.EvrService;
import gov.nist.policyserver.obligations.exceptions.InvalidEntityException;
import gov.nist.policyserver.obligations.exceptions.InvalidEvrException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.policyserver.common.Constants.*;

public class NodeService extends Service{

    private        AssignmentService assignmentService = new AssignmentService();
    private        AnalyticsService  analyticsService  = new AnalyticsService();
    private        EvrService        evrService        =  new EvrService();

    public HashSet<Node> getNodes(String namespace, String name, String type, String key, String value, String session, long process)
            throws InvalidNodeTypeException, InvalidPropertyException, ClassNotFoundException, SQLException, DatabaseException, IOException, SessionDoesNotExistException, SessionUserNotFoundException {
        Node user = getSessionUser(session);

        HashSet<Node> nodes = getNodes(namespace, name, type, key, value);

        nodes.removeIf(node -> {
            try {
                if(node.getType().equals(NodeType.OBJECT) || node.getType().equals(NodeType.OBJECT_ATTRIBUTE)) {
                    analyticsService.checkPermissions(user, process, node.getId(), ANY_OPERATIONS);
                }
                return false;
            }
            catch (MissingPermissionException | NoSubjectParameterException | InvalidProhibitionSubjectTypeException | NodeNotFoundException | ClassNotFoundException | ConfigurationException | DatabaseException | SQLException | InvalidPropertyException | IOException e) {
                return true;
            }
        });

        return nodes;
    }

    public HashSet<Node> getNodes(String namespace, String name, String type, String key, String value) throws InvalidNodeTypeException, InvalidPropertyException, ClassNotFoundException, SQLException, DatabaseException, IOException {
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
            throws InvalidNodeTypeException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        NodeType nodeType = (type != null) ? NodeType.toNodeType(type) : null;

        HashSet<Node> nodes = getGraph().getNodes();

        if(properties != null) {
            for (Property prop : properties) {
                if (prop.getKey().equals(NAMESPACE_PROPERTY)) {
                    namespace = prop.getValue();
                }
            }
        }

        final String fNamespace = namespace;
        //check namespace match
        if(namespace != null){
            nodes.removeIf(node -> {
                try {
                    return !node.hasProperty(NAMESPACE_PROPERTY) || !node.getProperty(NAMESPACE_PROPERTY).getValue()
                            .equalsIgnoreCase(fNamespace);
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
            throws InvalidNodeTypeException, UnexpectedNumberOfNodesException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
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

    public Node getNode(String name, String type, String properties, String session, long process) throws InvalidPropertyException, InvalidNodeTypeException, UnexpectedNumberOfNodesException, ClassNotFoundException, SQLException, IOException, DatabaseException, SessionDoesNotExistException, SessionUserNotFoundException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, NodeNotFoundException, MissingPermissionException {
        Node user = getSessionUser(session);

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
        Node node = getNode(null, name, type, propList);

        analyticsService.checkPermissions(user, process, node.getId(), ANY_OPERATIONS);

        return node;
    }

    public Node createNodeIn(long baseId, String name, String type, Property[] properties, String content, String session, long process) throws DatabaseException, NodeNotFoundException, IOException, SQLException, InvalidPropertyException, ClassNotFoundException, NullNameException, NullTypeException, InvalidNodeTypeException, InvalidAssignmentException, NodeIdExistsException, NodeNameExistsException, ConfigurationException, SessionDoesNotExistException, SessionUserNotFoundException, NoSubjectParameterException, InvalidProhibitionSubjectTypeException, UnexpectedNumberOfNodesException, AssociationExistsException, AssignmentExistsException, PropertyNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException, MissingPermissionException {
        Node user = getSessionUser(session);

        //check parent node exists
        Node parentNode = getNode(baseId);

        //check parameters are not null
        if(name == null){
            throw new NullNameException();
        }
        if(type == null){
            throw new NullTypeException();
        }

        //create Node
        Node node = createNode(NEW_NODE_ID, name, type, properties);

        //create assignment
        try {
            assignmentService.createAssignment(session, process, node.getId(), parentNode.getId(), false);
        }
        catch (AssignmentExistsException | UnexpectedNumberOfNodesException | AssociationExistsException | MissingPermissionException e) {
            deleteNode(node.getId());
            throw e;
        }

        //check if requesting content
        if(content != null) {
            analyticsService.checkPermissions(user, process, node.getId(), FILE_WRITE);

            ImportFile importFile = ContentHelper.createNodeContents(user, process, node, content);
            if(importFile != null) {
                node.setContent(content);

                //update node properties
                List<Property> nodeProperties = node.getProperties();
                for (Property prop : nodeProperties) {
                    switch (prop.getKey()) {
                        case BUCKET_PROPERTY:
                            updateNodeProperty(node.getId(), BUCKET_PROPERTY, importFile.getBucket());
                            break;
                        case PATH_PROPERTY:
                            updateNodeProperty(node.getId(), PATH_PROPERTY, importFile.getPath());
                            break;
                        case CONTENT_TYPE_PROPERTY:
                            updateNodeProperty(node.getId(), CONTENT_TYPE_PROPERTY, importFile.getContentType());
                            break;
                        case SIZE_PROPERTY:
                            updateNodeProperty(node.getId(), SIZE_PROPERTY, String.valueOf(importFile.getSize()));
                            break;
                    }
                }
            }
        }

        return node;
    }

    public Node createNode(String name, String type, Property[] properties, String session, long process) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NullNameException, NodeIdExistsException, ConfigurationException, NodeNotFoundException, AssignmentExistsException, InvalidNodeTypeException, PropertyNotFoundException, AssociationExistsException, NodeNameExistsException, InvalidAssignmentException, UnexpectedNumberOfNodesException, NullTypeException {
        Node user = getSessionUser(session);

        Node node = createNode(NO_BASE_ID, NEW_NODE_ID, name, type, properties);

        //if the node is a PC, create an OA and UA for PC admin
        if (node.getType().equals(NodeType.POLICY_CLASS)) {
            //create OA
            Node oaNode = createNode(node.getId(), NEW_NODE_ID, node.getName(), NodeType.OBJECT_ATTRIBUTE.toString(), new Property[]{new Property(NAMESPACE_PROPERTY, node.getName())});

            //create UA
            Node uaNode = createNode(node.getId(), NEW_NODE_ID, node.getName() + " admin", NodeType.USER_ATTRIBUTE.toString(), new Property[]{new Property(NAMESPACE_PROPERTY, node.getName())});

            //assign U to UA
            new AssignmentService().createAssignment(user.getId(), uaNode.getId());

            //create association
            new AssociationsService().createAssociation(uaNode.getId(), oaNode.getId(), new HashSet<>(Collections.singleton(ALL_OPERATIONS)), true);
        }

        return node;
    }

    public Node createNode(long baseId, long id, String name, String type, Property[] properties) throws NullNameException, NullTypeException, InvalidNodeTypeException, InvalidPropertyException, DatabaseException, ConfigurationException, NodeNameExistsException, NodeIdExistsException, NodeNotFoundException, InvalidAssignmentException, AssignmentExistsException, IOException, ClassNotFoundException, SQLException, UnexpectedNumberOfNodesException, AssociationExistsException, PropertyNotFoundException {
        //check name and type are not null
        if(name == null){
            throw new NullNameException();
        }
        if(type == null){
            throw new NullTypeException();
        }

        if(id != NEW_NODE_ID) {
            //check if ID exists
            try {
                Node node = getNode(id);
                throw new NodeIdExistsException(id, node);
            }
            catch (NodeNotFoundException e) {/*expected exception*/}
        }

        HashSet<Node> nodes = getNodes(null, name, type, properties != null ? Arrays.asList(properties) : null);
        if(!nodes.isEmpty()) {
            throw new NodeNameExistsException(name);
        }

        //create node in database
        NodeType nt = NodeType.toNodeType(type);
        Node node = getDaoManager().getNodesDAO().createNode(id, name, nt);

        //add the node to the nodes
        getGraph().addNode(node);


        //add properties to the node
        try {
            node = addNodeProperties(node, properties);
        }
        catch (PropertyNotFoundException e) {
            e.printStackTrace();
        }

        if(baseId != NO_BASE_ID) {
            //if there is a base ID present assign the new node to it
            AssignmentService assignmentService = new AssignmentService();
            assignmentService.createAssignment(node.getId(), baseId);
        }

        return node;
    }

    /**
     * This method is only used when importing nodes through a configuration file
     * @param id
     * @param name
     * @param type
     * @param properties
     * @return A Node object
     */
    public Node createNode(long id, String name, String type, Property[] properties) throws NullNameException, NullTypeException, NodeIdExistsException, ClassNotFoundException, SQLException, InvalidPropertyException, IOException, DatabaseException, InvalidNodeTypeException, NodeNameExistsException, NodeNotFoundException, ConfigurationException, InvalidKeySpecException, NoSuchAlgorithmException {
        //create node in database

        NodeType nt = NodeType.toNodeType(type);
        Node newNode = getDaoManager().getNodesDAO().createNode(id, name, nt);

        //add the node to the nodes
        getGraph().addNode(newNode);

        //add properties to the node
        //setNodeProperties(newNode, properties);
        try {
            newNode = addNodeProperties(newNode, properties);
        }
        catch (PropertyNotFoundException e) {
            e.printStackTrace();
        }

        return newNode;
    }

    private void setNodeProperties(Node node, Property[] properties) throws DatabaseException, NodeNotFoundException, IOException, SQLException, InvalidPropertyException, ClassNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException {
        for(Property prop : properties) {
            if(prop.getKey().equals(PASSWORD_PROPERTY)) {
                //check ic password is already hashed, and hash it if not
                //this will occur when loading a configuration
                if(prop.getValue().length() != HASH_LENGTH) {
                    String hash = generatePasswordHash(prop.getValue());
                    prop.setValue(hash);
                }
            }

            //add property to node in database
            getDaoManager().getNodesDAO().addNodeProperty(node.getId(), prop);

            //add property to node in nodes
            node.addProperty(prop);
        }
        /*if(properties != null) {
            for(Property prop : properties) {
                if (prop.getKey().equals(PASSWORD_PROPERTY)) {
                    //check ic password is already hashed, and hash it if not
                    //this will occur when loading a configuration
                    if (prop.getValue().length() != HASH_LENGTH) {
                        String hash = generatePasswordHash(prop.getValue());
                        prop.setValue(hash);
                    }
                }
            }

            getDaoManager().getNodesDAO().setNodeProperties(node.getId(), properties);

            node.setProperties(Arrays.asList(properties));
        }*/
    }


    private Node addNodeProperties(Node node, Property[] properties) throws NodeNotFoundException, DatabaseException, ConfigurationException, InvalidPropertyException, PropertyNotFoundException, SQLException, IOException, ClassNotFoundException {
        if(properties != null) {
            for (Property property : properties) {
                if(property.validProperty()) {
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

    public Node getNode(long nodeId, boolean content, String session, long process) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NodeNotFoundException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, MissingPermissionException, PropertyNotFoundException, ProhibitionNameExistsException, InvalidEvrException, ProhibitionDoesNotExistException, InvalidEntityException, NullNameException, ProhibitionResourceExistsException, InvalidNodeTypeException {
        Node user = getSessionUser(session);

        Node node = getNode(nodeId);

        if(node.getType().equals(NodeType.OBJECT_ATTRIBUTE) || node.getType().equals(NodeType.OBJECT)) {
            //check user can access the node
            analyticsService.checkPermissions(user, process, nodeId, ANY_OPERATIONS);
        }

        if(content) {
            node.setContent(ContentHelper.getNodeContents(user, process, nodeId, node));

            // process file read
            evrService.processFileRead(node, user, process);
        }

        return node;
    }

    public Node getNode(long nodeId) throws NodeNotFoundException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        Node node = getGraph().getNode(nodeId);
        if(node == null){
            throw new NodeNotFoundException(nodeId);
        }

        return node;
    }

    public Node getNodeInNamespace(String namespace, String name, NodeType type) throws SQLException, IOException, ClassNotFoundException, InvalidPropertyException, DatabaseException, InvalidNodeTypeException, NameInNamespaceNotFoundException {
        HashSet<Node> nodes = getNodes(namespace, name, null, null);
        if(nodes.isEmpty()){
            throw new NameInNamespaceNotFoundException(namespace, name, type);
        }

        return nodes.iterator().next();
    }

    public Node getNodeInNamespace(String namespace, String name, NodeType type, String session, long process)
            throws NameInNamespaceNotFoundException, InvalidNodeTypeException, InvalidPropertyException, ClassNotFoundException, SQLException, IOException, DatabaseException, SessionDoesNotExistException, SessionUserNotFoundException {
        HashSet<Node> nodes = getNodes(namespace, name, null, null, null, session, process);
        if(nodes.isEmpty()){
            throw new NameInNamespaceNotFoundException(namespace, name, type);
        }

        return nodes.iterator().next();
    }

    public Node updateNode(long nodeId, String name, Property[] properties, String content, String session, long process) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, NodeNotFoundException, MissingPermissionException, PropertyNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException {
        Node user = getSessionUser(session);

        //check user can update the node
        //analyticsService.checkPermissions(user, process, nodeId, UPDATE_NODE);

        Node node = updateNode(nodeId, name, properties);

        // check if updating content
        if(content != null) {
            //update the node contents
            ImportFile importFile = ContentHelper.updateNodeContents(user, process, node, content);
            if(importFile != null) {
                node.setContent(content);

                //update node properties
                List<Property> nodeProperties = node.getProperties();
                for (Property prop : nodeProperties) {
                    switch (prop.getKey()) {
                        case BUCKET_PROPERTY:
                            updateNodeProperty(nodeId, BUCKET_PROPERTY, importFile.getBucket());
                            break;
                        case PATH_PROPERTY:
                            updateNodeProperty(nodeId, PATH_PROPERTY, importFile.getPath());
                            break;
                        case CONTENT_TYPE_PROPERTY:
                            updateNodeProperty(nodeId, CONTENT_TYPE_PROPERTY, importFile.getContentType());
                            break;
                        case SIZE_PROPERTY:
                            updateNodeProperty(nodeId, SIZE_PROPERTY, String.valueOf(importFile.getSize()));
                            break;
                    }
                }
            }
        }

        return node;
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

    public void deleteNode(long nodeId, String session, long process) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, NodeNotFoundException, MissingPermissionException {
        //PERMISSION CHECK
        //get user from username
        Node user = getSessionUser(session);

        //check node exists
        Node node = getNode(nodeId);

        if(node.getType().equals(NodeType.OBJECT_ATTRIBUTE) || node.getType().equals(NodeType.OBJECT)) {
            //check user can delete the node
            analyticsService.checkPermissions(user, process, nodeId, DELETE_NODE);
        }

        deleteNode(nodeId);
    }

    public void deleteNode(long nodeId) throws NodeNotFoundException, DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //check node exists
        getNode(nodeId);

        //delete node in db
        getDaoManager().getNodesDAO().deleteNode(nodeId);

        //delete node in database
        getGraph().deleteNode(nodeId);
    }

    public List<Property> getNodeProperties(long nodeId) throws NodeNotFoundException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
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

    public Property getNodeProperty(long nodeId, String key) throws NodeNotFoundException, PropertyNotFoundException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        //get node
        Node node = getNode(nodeId);

        //get node property
        return node.getProperty(key);
    }

    public void deleteNodeProperty(long nodeId, String key, String session, long process) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NodeNotFoundException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, MissingPermissionException, PropertyNotFoundException {
        Node user = getSessionUser(session);

        Node node = getNode(nodeId);

        //check user can delete the node property
        analyticsService.checkPermissions(user, process, node.getId(), UPDATE_NODE);

        deleteNodeProperty(nodeId, key);
    }

    public void deleteNodeProperty(long nodeId, String key) throws NodeNotFoundException, PropertyNotFoundException, DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //check if the property exists
        getNodeProperty(nodeId, key);

        //delete the node property
        getDaoManager().getNodesDAO().deleteNodeProperty(nodeId, key);

        //delete node from the nodes
        getGraph().deleteNodeProperty(nodeId, key);
    }

    private void deleteNodeProperties(long nodeId) throws NodeNotFoundException, DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        List<Property> props = getNodeProperties(nodeId);

        for(Property property : props) {
            getDaoManager().getNodesDAO().deleteNodeProperty(nodeId, property.getKey());
        }

        getGraph().deleteNodeProperties(nodeId);
    }

    public void updateNodeProperty(long nodeId, String key, String value) throws NodeNotFoundException, PropertyNotFoundException, DatabaseException, InvalidKeySpecException, NoSuchAlgorithmException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
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

    public HashSet<Node> getChildrenOfType(long nodeId, String childType) throws NodeNotFoundException,
            InvalidNodeTypeException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        Node node = getNode(nodeId);

        HashSet<Node> children = getGraph().getChildren(node);
        HashSet<Node> retChildren = new HashSet<>(children);
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

    public HashSet<Node> getNodeChildren(long nodeId, String childType, String session, long process) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NodeNotFoundException, NoUserParameterException, ConfigurationException, InvalidNodeTypeException {
        Node user = getSessionUser(session);

        Node node = getNode(nodeId);

        HashSet<Node> nodes = new HashSet<>();

        if(node.getType().equals(NodeType.POLICY_CLASS)) {
            HashSet<Node> uas = getChildrenOfType(nodeId, NodeType.USER_ATTRIBUTE.toString());
            //add all uas because there are no associations on them
            nodes.addAll(uas);

            //get all oas user has access to
            List<PmAnalyticsEntry> accessibleChildren = analyticsService.getAccessibleChildren(nodeId, user.getId());
            for(PmAnalyticsEntry entry : accessibleChildren) {
                if(childType == null || childType.equals(entry.getTarget().getType().toString())) {
                    nodes.add(entry.getTarget());
                }
            }
        } else if(node.getType().equals(NodeType.OBJECT_ATTRIBUTE)) {
            //get all oas user has access to
            List<PmAnalyticsEntry> accessibleChildren = analyticsService.getAccessibleChildren(nodeId, user.getId());
            for(PmAnalyticsEntry entry : accessibleChildren) {
                if(childType == null || childType.equals(entry.getTarget().getType().toString())) {
                    nodes.add(entry.getTarget());
                }
            }
        } else if(node.getType().equals(NodeType.USER_ATTRIBUTE)) {
            //add all children
            HashSet<Node> children = getChildrenOfType(node.getId(), null);
            nodes.addAll(children);
        }

        return nodes;
    }

    public void deleteNodeChildren(long nodeId, String childType, String session, long process) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NodeNotFoundException, InvalidNodeTypeException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, MissingPermissionException {
        Node user = getSessionUser(session);

        analyticsService.checkPermissions(user, process, nodeId, DEASSIGN_FROM);

        HashSet<Node> children = getChildrenOfType(nodeId, childType);
        for(Node node : children) {
            analyticsService.checkPermissions(user, process, node.getId(), DELETE_NODE);
            analyticsService.checkPermissions(user, process, node.getId(), DEASSIGN);
        }

        deleteNodeChildren(nodeId, childType);
    }

    public void deleteNodeChildren(long nodeId, String childType) throws NodeNotFoundException, InvalidNodeTypeException, DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        HashSet<Node> children = getChildrenOfType(nodeId, childType);
        for(Node node : children){
            //delete node in db
            getDaoManager().getNodesDAO().deleteNode(node.getId());

            //delete node in database
            getGraph().deleteNode(node.getId());
        }
    }

    public HashSet<Node> getParentsOfType(long nodeId, String parentType, String session, long process) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, InvalidNodeTypeException, NodeNotFoundException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, MissingPermissionException {
        Node user = getSessionUser(session);

        HashSet<Node> parents = getParentsOfType(nodeId, parentType);
        parents.removeIf(node -> {
            try {
                analyticsService.checkPermissions(user, process, node.getId(), ANY_OPERATIONS);
                return false;
            } catch(Exception e) {
                return true;
            }
        });

        return parents;
    }

    public HashSet<Node> getParentsOfType(long nodeId, String parentType) throws InvalidNodeTypeException,
            NodeNotFoundException, ClassNotFoundException, SQLException, InvalidPropertyException, IOException, DatabaseException {
        Node node = getNode(nodeId);

        HashSet<Node> parents = getGraph().getParents(node);
        HashSet<Node> retParents = new HashSet<>(parents);
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
