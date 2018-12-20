package gov.nist.csd.pm.demos.ndac.algorithms.parsing.v2;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.prohibitions.Prohibition;
import gov.nist.csd.pm.common.model.prohibitions.ProhibitionNode;

import java.util.*;

public class BatchProhibitionDecider {

    private Graph                   graph;
    private Collection<Prohibition> prohibitions;

    public BatchProhibitionDecider(Graph graph, Collection<Prohibition> prohibitions) {
        if(prohibitions == null) {
            this.prohibitions = new ArrayList<>();
        }

        this.graph = graph;
        this.prohibitions = prohibitions;
    }

    public HashMap<Long, HashSet<String>> listProhibitedPermissions(long subjectID, Set<Long> targets) throws PMException {
        HashMap<Long, HashSet<String>> prohibitedOps = new HashMap<>();

        //if the subject ID or target ID are 0, return an empty set
        //if the IDs are 0, then the node doesn't exist in the graph
        //and therefore can't have prohibited ops (both subject and target)
        if(subjectID == 0 || targets == null || targets.isEmpty()) {
            return prohibitedOps;
        }

        for(Prohibition prohibition : prohibitions){
            boolean matches = (prohibition.getSubject().getSubjectID()==subjectID) ||
                    getSubGraph(prohibition.getSubject().getSubjectID()).contains(subjectID);
            if(matches){
                boolean inter = prohibition.isIntersection();
                List<ProhibitionNode> nodes = prohibition.getNodes();

                HashMap<ProhibitionNode, HashSet<Long>> drSubGraph = new HashMap<>();
                HashSet<Long> nodeIDs = new HashSet<>();
                for (ProhibitionNode dr : nodes) {
                    HashSet<Long> subGraph = getSubGraph(dr.getID());
                    drSubGraph.put(dr, subGraph);
                    nodeIDs.addAll(subGraph);
                }

                for(long targetID : targets) {
                    boolean addOps = false;
                    if (inter) {
                        for (ProhibitionNode dr : drSubGraph.keySet()) {
                            if (dr.isComplement()) {
                                nodeIDs.removeAll(drSubGraph.get(dr));
                            }
                        }
                        if (nodeIDs.contains(targetID)) {
                            addOps = true;
                        }
                    }
                    else {
                        addOps = true;
                        for (ProhibitionNode dr : drSubGraph.keySet()) {
                            HashSet<Long> subGraph = drSubGraph.get(dr);
                            if (dr.isComplement()) {
                                if (subGraph.contains(targetID)) {
                                    addOps = false;
                                }
                            }
                            else {
                                if (!subGraph.contains(targetID)) {
                                    addOps = false;
                                }
                            }
                        }
                    }

                    if (addOps) {
                        prohibitedOps.put(targetID, prohibition.getOperations());
                    }
                }
            }
        }

        return prohibitedOps;
    }

    private HashSet<Long> getSubGraph(long id) throws PMException {
        HashSet<Long> nodes = new HashSet<>();
        HashSet<Node> children = graph.getChildren(id);
        if(children.isEmpty()){
            return nodes;
        }

        //add all the children to the set of nodes
        for(Node node : children) {
            nodes.add(node.getID());
        }

        //for each child add it's subgraph
        for(Node child : children){
            nodes.addAll(getSubGraph(child.getID()));
        }

        return nodes;
    }
}
