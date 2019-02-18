package gov.nist.csd.pm.pdp.engine;

import gov.nist.csd.pm.common.exceptions.PMDBException;
import gov.nist.csd.pm.common.exceptions.PMGraphException;
import gov.nist.csd.pm.pap.graph.Graph;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.common.model.prohibitions.Prohibition;
import gov.nist.csd.pm.common.model.prohibitions.ProhibitionNode;

import java.util.*;

/**
 * This implementation of the ProhibitionDecider interface, stores prohibitions as a list of prohibition objects. Processing
 * the prohibitions is made faster by storing them in memory.
 */
public class MemProhibitionDecider implements ProhibitionDecider {

    private Graph                   graph;
    private Collection<Prohibition> prohibitions;

    public MemProhibitionDecider(Graph graph, Collection<Prohibition> prohibitions) {
        if(prohibitions == null) {
            this.prohibitions = new ArrayList<>();
        }

        this.graph = graph;
        this.prohibitions = prohibitions;
    }

    @Override
    public HashSet<String> listProhibitedPermissions(long subjectID, long targetID) throws PMDBException, PMGraphException {
        HashSet<String> prohibitedOps = new HashSet<>();

        //if the subject ID or target ID are 0, return an empty set
        //if the IDs are 0, then the node doesn't exist in the graph
        //and therefore can't have prohibited ops (both subject and target)
        if(subjectID == 0 || targetID == 0) {
            return prohibitedOps;
        }

        // iterate over all prohibitions
        for(Prohibition prohibition : prohibitions){
            // check if the subject provided matches the current prohibition's subject
            boolean matches = (prohibition.getSubject().getSubjectID()==subjectID) ||
                    getSubGraph(prohibition.getSubject().getSubjectID()).contains(subjectID);
            // if the subjects do match, continue processing the current prohibition
            if(matches){
                boolean inter = prohibition.isIntersection();
                List<ProhibitionNode> nodes = prohibition.getNodes();

                HashMap<ProhibitionNode, HashSet<Long>> drSubGraph = new HashMap<>();
                HashSet<Long> nodeIDs = new HashSet<>();
                // iterate over all nodes that are part of this prohibition
                // collect all of the nodes that are descendants of the node (entire subgraph with the current node as the root)
                for (ProhibitionNode dr : nodes) {
                    HashSet<Long> subGraph = getSubGraph(dr.getID());
                    drSubGraph.put(dr, subGraph);

                    // store all IDs collected
                    nodeIDs.addAll(subGraph);
                }

                // determine if the operations specified in this prohibition should be added to the set of prohibited ops
                boolean addOps = false;
                if(inter) {
                    // if the prohibition's intersection property is true
                    // iterate over all the nodes in the prohibition
                    for (ProhibitionNode dr : drSubGraph.keySet()) {
                        // if the current node's complement property is true
                        // remove it and all of it's descendants from the list of IDs.
                        if (dr.isComplement()) {
                            nodeIDs.removeAll(drSubGraph.get(dr));
                        }
                    }

                    // if after removing complemented nodes, the set of node IDs still contains
                    // the target node ID, the prohibition's operations are prohibited for the subject on the target
                    if (nodeIDs.contains(targetID)) {
                        addOps = true;
                    }
                }else{
                    // if the prohibition is not an intersection, the target node needs to be
                    // contained in each of the prohibition nodes.
                    addOps = true;
                    for (ProhibitionNode dr : drSubGraph.keySet()) {
                        HashSet<Long> subGraph = drSubGraph.get(dr);
                        if (dr.isComplement()) {
                            if(subGraph.contains(targetID)){
                                addOps = false;
                            }
                        }else{
                            if(!subGraph.contains(targetID)){
                                addOps = false;
                            }
                        }
                    }
                }

                if(addOps){
                    prohibitedOps.addAll(prohibition.getOperations());
                }
            }
        }

        return prohibitedOps;
    }

    private HashSet<Long> getSubGraph(long id) throws PMGraphException, PMDBException {
        HashSet<Long> nodes = new HashSet<>();
        HashSet<NodeContext> children = graph.getChildren(id);
        if(children.isEmpty()){
            return nodes;
        }

        //add all the children to the set of nodes
        for(NodeContext node : children) {
            nodes.add(node.getID());
        }

        //for each child add it's subgraph
        for(NodeContext child : children){
            nodes.addAll(getSubGraph(child.getID()));
        }

        return nodes;
    }
}
