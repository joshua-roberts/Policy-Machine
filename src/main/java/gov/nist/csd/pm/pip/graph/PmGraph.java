package gov.nist.csd.pm.pip.graph;
import gov.nist.csd.pm.model.graph.Assignment;
import gov.nist.csd.pm.model.graph.Association;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.graph.NodeType;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DirectedMultigraph;

import java.io.Serializable;
import java.util.*;

public class PmGraph implements Serializable{
    DirectedGraph<Node, Assignment> graph;
    public PmGraph(){
        graph = new DirectedMultigraph<>(Assignment.class);
    }

    public synchronized void addNode(Node n){
        graph.addVertex(n);
    }

    public synchronized void deleteNode(long nodeID){
        Node n = getNode(nodeID);
        deleteNode(n);
    }

    public synchronized void deleteNode(Node node){
        graph.removeVertex(node);
    }

    public synchronized void addEdge(Node child, Node parent, Assignment edge){
        graph.addEdge(child, parent, edge);
    }

    public Node getNode(long nodeID){
        HashSet<Node> nodes = getNodes();
        for(Node n : nodes){
            if(n.getID() == nodeID){
                return n;
            }
        }
        return null;
    }

    public synchronized HashSet<Node> getChildren(long nodeID){
        Node n = getNode(nodeID);

        HashSet<Node> children = new HashSet<>();
        Set<Assignment> assignments = graph.incomingEdgesOf(n);
        for(Assignment edge : assignments){
            children.add(edge.getChild());
        }
        return children;
    }

    public synchronized HashSet<Node> getParents(long nodeID){
        Node n = getNode(nodeID);

        HashSet<Node> parents = new HashSet<>();
        Set<Assignment> assignments = graph.outgoingEdgesOf(n);
        for(Assignment edge : assignments){
            parents.add(edge.getParent());
        }
        return parents;
    }

    public synchronized HashSet<Node> getChildren(Node n){
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

    public synchronized HashSet<Node> getParents(Node n){
        HashSet<Node> parents = new HashSet<>();
        Set<Assignment> assignments = graph.outgoingEdgesOf(n);
        for(Assignment edge : assignments){
            parents.add(edge.getParent());
        }
        return parents;
    }

    public synchronized HashSet<Assignment> outgoingEdgesOf(Node n){
        return new HashSet<>(graph.outgoingEdgesOf(n));
    }

    public synchronized HashSet<Assignment> incomingEdgesOf(Node n){
        return new HashSet<>(graph.incomingEdgesOf(n));
    }

    public HashSet<Node> getNodesOfType(NodeType type){
        HashSet<Node> nodes = getNodes();
        nodes.removeIf(node -> !node.getType().equals(type));
        return nodes;
    }

    /**
     * When looking at a nodes, ascendants are the nodes above a given node.  i.e. the children, grandchildren, etc.
     * @param node the node to get the ascendants for
     * @return a HashSet of Nodes
     */
    public synchronized HashSet<Node> getAscesndants(Node node){
        HashSet<Node> ascendants = new HashSet<>();
        HashSet<Node> children = getChildren(node);
        if(children.isEmpty()){
            return ascendants;
        }

        ascendants.addAll(children);

        for(Node child : children){
            ascendants.addAll(getAscesndants(child));
        }

        return ascendants;
    }

    public synchronized HashSet<Node> getAscesndants(long nodeId){
        Node node = getNode(nodeId);
        HashSet<Node> ascendants = new HashSet<>();
        HashSet<Node> children = getChildren(node);
        if(children.isEmpty()){
            return ascendants;
        }

        ascendants.addAll(children);

        for(Node child : children){
            ascendants.addAll(getAscesndants(child));
        }

        return ascendants;
    }

    public synchronized HashSet<Node> getNodes(){
        HashSet<Node> nodes = new HashSet<>(graph.vertexSet());
        nodes.removeIf(node -> node.getType() == null);
        return nodes;
    }

    public synchronized void createAssignment(long childId, long parentId){
        Node child = getNode(childId);
        Node parent = getNode(parentId);
        graph.addEdge(child, parent, new Assignment<>(child, parent));
    }

    public synchronized void createAssignment(Node child, Node parent){
        graph.addEdge(child, parent, new Assignment<>(child, parent));
    }

    public synchronized void deleteAssignment(long childId, long parentId){
        Node child = getNode(childId);
        Node parent = getNode(parentId);
        graph.removeEdge(child, parent);
    }

    public HashSet<Assignment> getAssignments() {
        HashSet<Assignment> assignments = new HashSet<>();
        Set<Assignment> edges = graph.edgeSet();
        for(Assignment a : edges){
            if(!(a instanceof Association)){
                assignments.add(a);
            }
        }

        return assignments;
    }

    public synchronized void deleteAssignment(Node child, Node parent){
        graph.removeEdge(child, parent);
    }

    public synchronized void createAssociation(long uaId, long targetId, HashSet<String> operations){
        Node ua = getNode(uaId);
        Node target = getNode(targetId);
        graph.addEdge(ua, target, new Association<>(ua, target, operations));
    }

    public synchronized void createAssociation(Node ua, Node target, HashSet<String> operations){
        graph.addEdge(ua, target, new Association<>(ua, target, operations));
    }

    public synchronized void updateAssociation(long uaId, long targetId, HashSet<String> ops){
        Set<Assignment> edges = graph.getAllEdges(getNode(uaId), getNode(targetId));
        for(Assignment edge : edges){
            if(edge instanceof Association){
                ((Association) edge).setOperations(ops);
            }
        }
    }

    public synchronized void deleteAssociation(long uaId, long targetId){
        deleteAssignment(uaId, targetId);
    }

    public synchronized void deleteAssociation(Node ua, Node target){
        deleteAssignment(ua, target);
    }

    public synchronized Association getAssociation(long uaId, long targetId){
        return (Association) graph.getEdge(getNode(uaId), getNode(targetId));
    }

    public List<Association> getUattrAssociations(long uaId){
        List<Association> assocs = new ArrayList<>();
        Set<Assignment> assignments = graph.outgoingEdgesOf(getNode(uaId));
        for(Assignment edge : assignments){
            if(edge instanceof Association){
                Association assocEdge = (Association)edge;
                assocs.add(new Association(assocEdge.getChild(), assocEdge.getParent(), assocEdge.getOps()));
            }
        }
        return assocs;
    }

    public List<Association> getTargetAssociations(long targetId){
        List<Association> assocs = new ArrayList<>();
        Set<Assignment> assignments = graph.incomingEdgesOf(getNode(targetId));
        for(Assignment edge : assignments){
            if(edge instanceof Association){
                Association assocEdge = (Association)edge;
                assocs.add(new Association(assocEdge.getChild(), assocEdge.getParent(), assocEdge.getOps()));
            }
        }
        return assocs;
    }

    public synchronized boolean isAssigned(long childId, long parentId){
        Assignment edge = graph.getEdge(getNode(childId), getNode(parentId));
        return !(edge == null || edge instanceof Association);
    }

    public synchronized boolean isAssigned(Node child, Node parent){
        Assignment edge = graph.getEdge(child, parent);
        return !(edge == null || edge instanceof Association);
    }

    public List<Association> getAssociations() {
        List<Association> assocs = new ArrayList<>();
        Set<Assignment> edges = graph.edgeSet();
        for(Assignment assignment : edges){
            if(assignment instanceof Association){
                Association assocEdge = (Association)assignment;
                assocs.add(new Association(assocEdge.getChild(), assocEdge.getParent(), assocEdge.getOps()));
            }
        }
        return assocs;
    }

    public boolean isAssociated(Node ua, Node target) {
        Assignment assignment = graph.getEdge(ua, target);
        return assignment instanceof Association;
    }
}
