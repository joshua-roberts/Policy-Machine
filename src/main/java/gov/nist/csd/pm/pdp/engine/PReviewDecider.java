package gov.nist.csd.pm.pdp.engine;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.common.model.prohibitions.Prohibition;


import java.util.*;

import static gov.nist.csd.pm.common.constants.Operations.ALL_OPERATIONS;
import static gov.nist.csd.pm.common.constants.Operations.ANY_OPERATIONS;

/**
 * An implementation of the Decider interface that uses an in memory NGAC graph
 */
public class PReviewDecider implements Decider {

    private Graph              graph;
    private ProhibitionDecider prohibitionDecider;

    /**
     * Create a new Decider with with the given NGAC graph, user ID, and process ID.
     * @param graph The NGAC Graph to use in the policy decision.
     */
    public PReviewDecider(Graph graph, Collection<Prohibition> prohibitions) throws IllegalArgumentException {
        if (graph == null) {
            throw new IllegalArgumentException("NGAC graph cannot be null");
        } else if (prohibitions == null) {
            prohibitions = new ArrayList<>();
        }

        this.graph = graph;
        this.prohibitionDecider = new MemProhibitionDecider(graph, prohibitions);
    }

    @Override
    public boolean hasPermissions(long userID, long processID, long targetID, String... perms) throws SessionDoesNotExistException, LoadConfigException, MissingPermissionException, DatabaseException, NodeNotFoundException, InvalidProhibitionSubjectTypeException {
        List<String> permsToCheck = Arrays.asList(perms);
        HashSet<String> permissions = listPermissions(userID, processID, targetID);

        //if just checking for any operations, return true if the resulting permissions set is not empty.
        //if the resulting permissions set contains * or all operations, return true.
        //if neither of the above apply, return true iff the resulting permissions set contains all the provided
        // permissions to check for
        if(permsToCheck.contains(ANY_OPERATIONS)) {
            return !permissions.isEmpty();
        } else if(permissions.contains(ALL_OPERATIONS)) {
            return true;
        } else {
            return permissions.containsAll(permsToCheck);
        }
    }

    @Override
    public HashSet<String> listPermissions(long userID, long processID, long targetID) throws DatabaseException, LoadConfigException, SessionDoesNotExistException, MissingPermissionException, NodeNotFoundException, InvalidProhibitionSubjectTypeException {
        HashSet<String> perms = new HashSet<>();

        //walk the user side and get all target nodes reachable by the user through associations
        HashMap<Long, HashSet<String>> dc = getBorderTargets(userID);
        if(dc.isEmpty()){
            return perms;
        }


        HashMap<Long, HashMap<Long, HashSet<String>>> visitedNodes = new HashMap<>();
        HashSet<Long> pcs = graph.getPolicies();
        //visit the policy class nodes to signal the end of the dfs
        for(long pc : pcs){
            HashMap<Long, HashSet<String>> pcMap = new HashMap<>();
            pcMap.put(pc, new HashSet<>());
            visitedNodes.put(pc, pcMap);
        }

        //start a depth first search on the target node.
        dfs(targetID, visitedNodes, dc);

        //get the intersection of permissions the user has on the target in each policy class
        HashMap<Long, HashSet<String>> pcMap = visitedNodes.get(targetID);
        boolean addOps = true;
        for(long pc : pcMap.keySet()){
            HashSet<String> ops = pcMap.get(pc);
            if(ops.isEmpty()) {// if the ops for the pc are empty then the user has no permissions on the target
                perms.clear();
                break;
            } else if(addOps){// if this is the first time were adding ops just add to perms
                perms.addAll(ops);
                addOps = false;
            }else{// remove any ops that aren't in both sets
                perms.retainAll(ops);
            }
        }

        //remove permissions prohibited for the current user and process
        perms.removeAll(prohibitionDecider.listProhibitedPermissions(userID, targetID));
        perms.removeAll(prohibitionDecider.listProhibitedPermissions(processID, targetID));

        return perms;
    }

    @Override
    public HashSet<Node> filter(long userID, long processID, HashSet<Node> nodes, String... perms) {
        nodes.removeIf((n) -> {
            try {
                return !hasPermissions(userID, processID, n.getID(), perms);
            }
            catch (SessionDoesNotExistException | LoadConfigException | DatabaseException | MissingPermissionException | NodeNotFoundException | InvalidProhibitionSubjectTypeException e) {
                e.printStackTrace();
                return true;
            }
        });
        return nodes;
    }

    @Override
    public HashSet<Node> getChildren(long userID, long processID, long targetID, String... perms) throws SessionDoesNotExistException, LoadConfigException, DatabaseException, MissingPermissionException, NodeNotFoundException, InvalidProhibitionSubjectTypeException {
        HashSet<Node> children = graph.getChildren(targetID);
        return filter(userID, processID, children, perms);
    }

    /**
     * Find the target nodes that are reachable by the user via an Association. This is done by a breadth first search
     * starting at the user node and walking up the user side of the graph until all User Attributes the user is assigned
     * to have been visited.  For each User Attribute visited, get the associations it is the source of and store the
     * target of that association as well as the operations in a map. If a target node is reached multiple times, add any
     * new operations to the already existing ones.
     * @return A Map of target nodes that the user can reach via associations and the operations the user has on each.
     */
    private synchronized HashMap<Long, HashSet<String>> getBorderTargets(long userID) throws SessionDoesNotExistException, LoadConfigException, DatabaseException, MissingPermissionException, NodeNotFoundException, InvalidProhibitionSubjectTypeException {
        HashMap<Long, HashSet<String>> borderTargets = new HashMap<>();

        //get the parents of the user to start bfs on user side
        HashSet<Node> parents = graph.getParents(userID);
        while(!parents.isEmpty()){
            Node parentNode = parents.iterator().next();

            //get the associations the current parent node is the source of
            HashMap<Long, HashSet<String>> assocs = graph.getSourceAssociations(parentNode.getID());

            //collect the target and operation information for each association
            for (long targetID : assocs.keySet()) {
                HashSet<String> ops = assocs.get(targetID);
                HashSet<String> exOps = borderTargets.get(targetID);
                //if the target is not in the map already, put it
                //else add the found operations to the existing ones.
                if (exOps == null) {
                    borderTargets.put(targetID, ops);
                } else {
                    ops.addAll(exOps);
                    borderTargets.put(targetID, ops);
                }
            }

            //add all of the current parent node's parents to the queue
            parents.addAll(graph.getParents(parentNode.getID()));

            //remove the current parent from the queue
            parents.remove(parentNode);
        }

        return borderTargets;
    }

    /**
     * Perform a depth first search on the object side of the graph.  Start at the target node and recursively visit nodes
     * until a policy class is reached.  On each node visited, collect any operation the user has on the target. At the
     * end of each dfs iteration the visitedNodes map will contain the operations the user is permitted on the target under
     * each policy class.
     * @param targetID The ID of the current target node
     * @param visitedNodes The map of nodes that have been visited
     * @param borderTargets The target nodes reachable by the user via associations
     */
    private synchronized void dfs(long targetID, HashMap<Long, HashMap<Long, HashSet<String>>> visitedNodes, HashMap<Long, HashSet<String>> borderTargets) throws SessionDoesNotExistException, LoadConfigException, DatabaseException, MissingPermissionException, NodeNotFoundException, InvalidProhibitionSubjectTypeException {
        //visit the current target node
        visitedNodes.put(targetID, new HashMap<>());

        HashSet<Node> parents = graph.getParents(targetID);

        //iterate over the parents of the target node
        for(Node parent : parents){
            //if the parent has not been visited yet, make recursive call to dfs on it
            if(!visitedNodes.containsKey(parent.getID())){
                dfs(parent.getID(), visitedNodes, borderTargets);
            }

            //store all the operations and policy classes for this target node
            HashMap<Long, HashSet<String>> pcSet = visitedNodes.get(parent.getID());
            for(long pc : pcSet.keySet()){
                HashSet<String> ops = pcSet.get(pc);
                HashSet<String> exOps = visitedNodes.get(targetID).computeIfAbsent(pc, k -> new HashSet<>());
                exOps.addAll(ops);
            }
        }

        //if the target node is a border target, add the operations found during bfs
        if(borderTargets.containsKey(targetID)){
            HashMap<Long, HashSet<String>> pcSet = visitedNodes.get(targetID);
            HashSet<String> ops = borderTargets.get(targetID);
            for(long pcId : pcSet.keySet()){
                visitedNodes.get(targetID).get(pcId).addAll(ops);
            }
        }
    }

    private synchronized Node createVNode(HashMap<Long, HashSet<String>> dc) throws NullNameException, LoadConfigException, DatabaseException, NullTypeException, NullNodeException, NoIDException, SessionDoesNotExistException, MissingPermissionException, NodeNotFoundException, InvalidAssignmentException, InvalidProhibitionSubjectTypeException {
        Node vNode = new Node("VNODE", NodeType.OA);
        long vNodeID = graph.createNode(vNode);
        for(long nodeID : dc.keySet()){
            graph.assign(nodeID, NodeType.OA, vNode.getID(), NodeType.OA);
        }
        return vNode.id(vNodeID);
    }
}
