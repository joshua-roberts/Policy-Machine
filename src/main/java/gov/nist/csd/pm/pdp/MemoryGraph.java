package gov.nist.csd.pm.pdp;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.*;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DirectedMultigraph;

import java.io.Serializable;
import java.util.*;

public class MemoryGraph implements Serializable, Graph {
    DirectedGraph<Node, Assignment> graph;
    public MemoryGraph(){
        graph = new DirectedMultigraph<>(Assignment.class);
    }

    @Override
    public Node createUser(String name, HashMap<String, String> properties) throws NodeExistsException {
        return createNode(name, NodeType.USER, properties);
    }

    @Override
    public Node createUserAttribute(String name, HashMap<String, String> properties) throws NodeExistsException {
        return createNode(name, NodeType.USER, properties);
    }

    @Override
    public Node createObject(String name, HashMap<String, String> properties) throws NodeExistsException {
        return createNode(name, NodeType.USER, properties);
    }

    @Override
    public Node createObjectAttribute(String name, HashMap<String, String> properties) throws NodeExistsException {
        return createNode(name, NodeType.USER, properties);
    }

    @Override
    public Node createPolicyClass(String name, HashMap<String, String> properties) throws NodeExistsException {
        return createNode(name, NodeType.USER, properties);
    }

    private Node createNode(String name, NodeType type, HashMap<String, String> properties) throws NodeExistsException {
        Node node = new Node(name, NodeType.POLICY_CLASS, properties);

        //check if node already exists
        try {
            getNode(node.getID());
            throw new NodeExistsException(node);
        } catch (NodeNotFoundException e) {}

        graph.addVertex(node);
        return node;
    }

    @Override
    public void deleteNode(long id) throws NodeNotFoundException {
        Node node = getNode(id);

        graph.removeVertex(getNode(id));
    }

    @Override
    public Node getNode(long id) throws NodeNotFoundException {
        Set<Node> nodes = graph.vertexSet();
        for(Node n : nodes){
            if(n.getID() == id){
                return n;
            }
        }

        throw new NodeNotFoundException(id);
    }

    @Override
    public void createAssignment(long childID, long parentID) throws NodeNotFoundException, AssignmentExistsException, InvalidAssignmentException {
        Node child = getNode(childID);
        Node parent = getNode(parentID);

        if (isAssigned(child, parent) ) {
            throw new AssignmentExistsException("Assignment exists between node " + childID + " and " + parentID);
        }

        Assignment.checkAssignment(child.getType(), parent.getType());

        graph.addEdge(child, parent);
    }

    private synchronized boolean isAssigned(Node child, Node parent){
        Assignment edge = graph.getEdge(child, parent);
        return !(edge == null || edge instanceof Association);
    }

    @Override
    public void deleteAssignment(long childID, long parentID) throws NodeNotFoundException, AssignmentDoesNotExistException {
        Node child = getNode(childID);
        Node parent = getNode(parentID);

        if (!isAssigned(child, parent) ) {
            throw new AssignmentDoesNotExistException(childID, parentID);
        }

        graph.removeEdge(child, parent);
    }

    @Override
    public HashSet<Node> getChildren(long id) throws NodeNotFoundException {
        Node n = getNode(id);
        HashSet<Node> children = new HashSet<>();
        Set<Assignment> assignments = graph.incomingEdgesOf(n);
        for(Assignment edge : assignments){
            if(edge instanceof Association) {
                continue;
            }
            children.add(edge.getChild());
        }
        return children;
    }

    @Override
    public HashSet<Node> getParents(long id) throws NodeNotFoundException {
        Node n = getNode(id);
        HashSet<Node> parents = new HashSet<>();
        Set<Assignment> assignments = graph.outgoingEdgesOf(n);
        for(Assignment edge : assignments){
            parents.add(edge.getParent());
        }
        return parents;
    }

    @Override
    public void createAssociation(long uaID, long targetID, String... operations) throws NodeNotFoundException, AssociationExistsException, InvalidAssociationException {
        Node ua = getNode(uaID);
        Node target = getNode(targetID);

        if (isAssociated(ua, target)) {
            throw new AssociationExistsException(uaID, targetID);
        }

        Association.checkAssociation(ua.getType(), target.getType());

        graph.addEdge(ua, target, new Association<Node>(ua, target, new HashSet<>(Arrays.asList(operations))));
    }

    @Override
    public void updateAssociation(long uaID, long targetID, String... operations) throws NodeNotFoundException, AssociationDoesNotExistException {
        Node ua = getNode(uaID);
        Node target = getNode(targetID);

        if (!isAssociated(ua, target)) {
            throw new AssociationDoesNotExistException(uaID, targetID);
        }

        Set<Assignment> edges = graph.getAllEdges(ua, target);
        if (edges != null && edges.size() > 0) {
            Association association = (Association) edges.iterator().next();
            association.setOperations(new HashSet<>(Arrays.asList(operations)));
        }
    }

    @Override
    public void deleteAssociation(long uaID, long targetID) throws NodeNotFoundException, AssociationDoesNotExistException {
        Node ua = getNode(uaID);
        Node target = getNode(targetID);

        if (!isAssociated(ua, target)) {
            throw new AssociationDoesNotExistException(uaID, targetID);
        }

        graph.removeEdge(ua, target);
    }

    private boolean isAssociated(Node ua, Node target) {
        Assignment assignment = graph.getEdge(ua, target);
        return assignment instanceof Association;
    }

    @Override
    public List<Association> getAssociations(long uaID) throws NodeNotFoundException {
        Node ua = getNode(uaID);

        List<Association> associations = new ArrayList<>();
        Set<Assignment> assignments = graph.outgoingEdgesOf(ua);
        for(Assignment assignment : assignments) {
            if(assignment instanceof Association) {
                associations.add((Association) assignment);
            }
        }

        return associations;
    }
}
