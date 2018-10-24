package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.demos.ndac.translator.exceptions.PMAccessDeniedException;
import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pdp.engine.PolicyDecider;
import gov.nist.csd.pm.pip.loader.LoaderException;
import gov.nist.csd.pm.model.graph.NGACNodeContext;
import gov.nist.csd.pm.model.graph.Search;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.csd.pm.model.constants.Operations.*;
import static gov.nist.csd.pm.model.constants.Properties.NAMESPACE_PROPERTY;
import static gov.nist.csd.pm.pip.PIP.getPIP;

/**
 * Service class for the Nodes resource.
 */
public class NodeService extends Service {

    /**
     * Create a new NodeService with the given sessionID and processID.
     */
    public NodeService(String sessionID, long processID) {
        super(sessionID, processID);
    }

    /**
     * Search for the nodes that match the given parameters. Uses the Search interface to retrieve nodes from the database.
     * @param name The name of the nodes to search for.
     * @param type The type of the nodes to search for.
     * @param properties The properties of nodes to search for.
     * @return A set of NGACNodes the have the name, type, and properties provided.
     */
    public Set<Node> getNodes(String name, String type, Map<String, String> properties)
            throws LoaderException, DatabaseException, LoadConfigException, SessionDoesNotExistException {
        // get the nodes according the search criteria
        HashSet<Node> nodes = getPIP()
                .getNGACBackend()
                .getSearch()
                .search(name, type, properties);


        // instantiate a new PolicyDecider to filter the nodes the current uer has access to.
        PolicyDecider decider = getPolicyDecider();
        return decider.filter(nodes, ANY_OPERATIONS);
    }

    /**
     * Get a single node with the given name, type, and properties. Makes a call to getNodes(name, type, properties).
     * @throws UnexpectedNumberOfNodesException If there are 0 or more than one nodes returned using the provided search criteria.
     */
    public Node getNode(String name, String type, Map<String, String> properties)
            throws PMException {
        Set<Node> nodes = getNodes(name, type, properties);

        if (nodes.size() != 1) {
            throw new UnexpectedNumberOfNodesException();
        }

        return nodes.iterator().next();
    }

    /**
     * Create a node and assign it to the node represented by the baseID parameter.
     * @param baseID The ID of the node to assign the newly created node in.
     * @param name The name of the node to create.
     * @param type The type of the node to create.
     * @param properties The properties to add to the node.
     * @return
     * @throws PMException
     */
    public Node createNodeIn(long baseID, String name, String type, HashMap<String, String> properties) throws PMException {
        //check parameters are not null
        if (name == null) {
            throw new NullNameException();
        }
        if (type == null) {
            throw new NullTypeException();
        }

        //check parent node exists
        Node parentNode = getNode(baseID);

        // check if this name and type already exists in the namespace
        HashMap<String, String> nsProp = new HashMap<>();
        nsProp.put(NAMESPACE_PROPERTY, properties.get(NAMESPACE_PROPERTY));
        Set<Node> nodes = getNodes(name, type, nsProp);
        if (!nodes.isEmpty()) {
            throw new NodeNameExistsInNamespaceException(nsProp.get(NAMESPACE_PROPERTY), name);
        }

        //create OldNode
        Node node = createNode(name, type, properties);

        //create assignment
        try {
            AssignmentService assignmentService = new AssignmentService(getSessionID(), getProcessID());
            assignmentService.createAssignment(node, parentNode);
        }
        catch (AssociationExistsException | InvalidAssociationException e) {
            // if the assignment could not be created, delete this node and throw the exception
            deleteNode(node.getID());
            throw e;
        }

        return node;
    }

    /**
     * This method creates a new Policy Class in the NGAC graph.  Unlike other nodes, Policy Class nodes must have unique names.
     * When a policy class is created, an object attribute and a user attribute of the same name are also created and assigned
     * to the policy class. The current user is then assigned to the user attribute and an association is created to give the current
     * user all operations (*) on the object attribute.  Finally, the super user attribute is associated with the object
     * attribute created above, to give the super user all operations as well.
     *
     * @param name       The name of the Policy Class
     * @param properties Any additional properties to add to the Policy Class
     * @return A OldNode object representing the Policy Class that was added to the graph
     */
    public Node createPolicy(String name, HashMap<String, String> properties) throws PMException {
        if (properties == null) {
            properties = new HashMap<>();
        }

        //check that the PC name does not exist
        Set<Node> nodes = getNodes(name, NodeType.PC.toString(), null);
        if (!nodes.isEmpty()) {
            throw new PolicyClassNameExistsException(name);
        }

        //create the PC node
        Node node = createNode(name, NodeType.PC.toString(), properties);

        //create OA
        Node oaNode = createNodeIn(node.getID(), node.getName() + " OA", NodeType.OA.toString(), properties);

        //create UA
        Node uaNode = createNodeIn(node.getID(), node.getName() + " admin", NodeType.UA.toString(), properties);

        //assign U to UA
        AssignmentService assignmentService = new AssignmentService(getSessionID(), getProcessID());
        assignmentService.createAssignment(getNode(getSessionUserID()), uaNode);

        //create association for the admin UA
        AssociationsService associationsService = new AssociationsService();
        associationsService.createAssociation(uaNode.getID(), oaNode.getID(), new HashSet<>(Collections.singleton(ALL_OPERATIONS)));
        //create association for super UA
        associationsService.createAssociation(getSuperUAID(), oaNode.getID(), new HashSet<>(Collections.singleton(ALL_OPERATIONS)));

        return node;
    }

    /**
     * Create a node in the database and memory graph
     * @param name
     * @param type
     * @param properties
     * @return
     * @throws PMException
     */
    protected Node createNode(String name, String type, HashMap<String, String> properties) throws PMException {
        if (properties == null) {
            properties = new HashMap<>();
        }

        // convert the string type to NodeType
        NodeType nt = NodeType.toNodeType(type);

        //create an NGAC OldNode context
        NGACNodeContext ctx = new NGACNodeContext(nt).name(name).properties(properties);

        //create node in database
        Node newNode = getDB().createNode(ctx);
        //create node in memory
        getMem().createNode(ctx);

        return newNode;
    }

    /**
     * Get the OldNode with the given ID.
     * @param nodeID The ID of the node to return.
     * @return The OldNode with the given ID.
     * @throws LoadConfigException
     * @throws DatabaseException
     * @throws LoaderException
     * @throws NodeNotFoundException
     * @throws InvalidNodeTypeException
     */
    private Node getNode(long nodeID) throws LoadConfigException, DatabaseException, LoaderException, NodeNotFoundException, InvalidNodeTypeException {
        Search search = getSearch();
        return search.getNode(nodeID);
    }

    /**
     * Delete the node with the given ID.  Before deleting check that the current user is allowed to delete the node.
     * If the node does not exist, no exception is thrown.
     * @param nodeID the ID of the node.
     */
    public void deleteNode(long nodeID) throws PMException {
        //delete node in db
        getDB().deleteNode(nodeID);

        //delete node in database
        getMem().deleteNode(nodeID);
    }

    public HashSet<Node> getChildrenOfType(long nodeID, String childType) throws PMException {
        HashSet<Node> children = getDB().getChildren(nodeID);
        HashSet<Node> retChildren = new HashSet<>(children);
        if (childType != null) {
            NodeType nt = NodeType.toNodeType(childType);
            for (Node n : children) {
                if (!n.getType().equals(nt)) {
                    retChildren.remove(n);
                }
            }
        }
        return retChildren;
    }

    public HashSet<Node> getNodeChildren(long nodeID, String childType, String session, long process) throws PMException {
        Node node = getNode(nodeID);

        PolicyDecider decider = getPolicyDecider();

        HashSet<Node> nodes = new HashSet<>();
        if (node.getType().equals(NodeType.PC)) {
            HashSet<Node> uas = getChildrenOfType(nodeID, NodeType.UA.toString());
            //add all uas because there are no associations on them
            nodes.addAll(uas);

            //get all oas user has access to
            HashSet<Node> oas = getChildrenOfType(nodeID, NodeType.OA.toString());
            oas = decider.filter(oas, ANY_OPERATIONS);
            nodes.addAll(oas);
        }
        else if (node.getType().equals(NodeType.OA)) {
            //get all oas user has access to
            HashSet<Node> oas = getChildrenOfType(nodeID, NodeType.OA.toString());
            oas = decider.filter(oas, ANY_OPERATIONS);
            nodes.addAll(oas);
        }
        else if (node.getType().equals(NodeType.UA)) {
            HashSet<Node> uas = getChildrenOfType(nodeID, NodeType.UA.toString());
            nodes.addAll(uas);
        }

        return nodes;
    }

    public void deleteNodeChildren(long nodeID, String childType) throws PMException, IOException, SQLException, ClassNotFoundException {
        //check that the node exists
        boolean exists = getMem().exists(nodeID);
        if (!exists) {
            throw new NodeNotFoundException(nodeID);
        }

        //check that the current user can deassign from the node
        PolicyDecider decider = getPolicyDecider();
        if(!decider.hasPermissions(nodeID, DEASSIGN_FROM)) {
            throw new PMAccessDeniedException(nodeID);
        }

        //get the children of the node and delete those that the user is able to
        HashSet<Node> children = getChildrenOfType(nodeID, childType);
        for (Node node : children) {
            if(!decider.hasPermissions(node.getID(), DELETE_NODE)) {
                continue;
            }

            if(!decider.hasPermissions(node.getID(), DEASSIGN)) {
                continue;
            }

            //delete node in db
            getDB().deleteNode(node.getID());

            //delete node in database
            getMem().deleteNode(node.getID());
        }
    }

    public HashSet<Node> getParentsOfType(long nodeID, String parentType) throws PMException {
        HashSet<Node> parents = getDB().getParents(nodeID);
        HashSet<Node> retParents = new HashSet<>(parents);
        if (parentType != null) {
            NodeType nt = NodeType.toNodeType(parentType);
            for (Node n : parents) {
                if (!n.getType().equals(nt)) {
                    retParents.remove(n);
                }
            }
        }

        PolicyDecider decider = getPolicyDecider();
        return decider.filter(retParents, ANY_OPERATIONS);
    }
}
