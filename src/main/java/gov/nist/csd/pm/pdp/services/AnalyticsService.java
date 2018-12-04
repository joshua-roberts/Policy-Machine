package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.Search;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;

import gov.nist.csd.pm.pdp.engine.MemPolicyDecider;
import gov.nist.csd.pm.pdp.engine.PolicyDecider;

import java.util.*;

/**
 * Methods to analyze the NGAC data.
 */
public class AnalyticsService extends Service {

    public AnalyticsService(String sessionID, long processID) {
        super(sessionID, processID);
    }

    /**
     * Given the ID of a target node, return the permissions the current user has on it.
     * @param targetID The ID of the target node.
     * @return The set of operations the current user has on the target node.
     */
    public HashSet<String> getPermissions(long targetID) throws SessionDoesNotExistException, LoadConfigException, DatabaseException, NodeNotFoundException, MissingPermissionException, InvalidProhibitionSubjectTypeException {
        PolicyDecider decider = new MemPolicyDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
        return decider.listPermissions(getSessionUserID(), getProcessID(), targetID);
    }


    /**
     * Get the Personal Object System for the user of the current session.  This method returns the first level of nodes
     * the user has direct access to.
     * @return The set of nodes that the user has direct access.
     * @throws SessionDoesNotExistException
     * @throws DatabaseException
     * @throws LoadConfigException
     * @throws LoaderException
     * @throws NodeNotFoundException
     * @throws MissingPermissionException
     */
    public HashSet<Node> getPos() throws SessionDoesNotExistException, DatabaseException, LoadConfigException, NodeNotFoundException, MissingPermissionException, InvalidNodeTypeException, InvalidProhibitionSubjectTypeException {
        // Prepare the hashset to return.
        HashSet<Long> hsOa = new HashSet<>();

        // Call find_border_oa_priv(u). The result is a Hashtable
        // htoa = {oa -> {op -> pcset}}:
        Hashtable htOa = findBorderOaPrivRestrictedInternal();

        // For each returned oa (key in htOa)
        for (Enumeration oas = htOa.keys(); oas.hasMoreElements(); ) {
            long oaID = (long)oas.nextElement();

            // Compute oa's required PCs by calling find_pc_set(sOaID).
            HashSet hsReqPcs = inMemFindPcSet(oaID, NodeType.OA);
            // Extract oa's label.
            Hashtable htOaLabel = (Hashtable)htOa.get(oaID);

            // Walk through the op -> pcset of the oa's label.
            // For each operation/access right
            for (Enumeration ops = htOaLabel.keys(); ops.hasMoreElements(); ) {
                String sOp = (String)ops.nextElement();
                // Extract the pcset corresponding to this operation/access right.
                HashSet hsActualPcs = (HashSet)htOaLabel.get(sOp);
                // if the set of required PCs is a subset of the actual pcset,
                // then user u has some privileges on the current oa node.
                if (hsActualPcs.containsAll(hsReqPcs)) {
                    hsOa.add(oaID);
                    break;
                }
            }
        }

        HashSet<Node> nodes = new HashSet<>();
        Search search = getSearch();
        for(Long id : hsOa) {
            Node node = search.getNode(id);
            nodes.add(node);
        }

        return nodes;
    }

    private Hashtable findBorderOaPrivRestrictedInternal() throws SessionDoesNotExistException, LoadConfigException, DatabaseException, NodeNotFoundException, MissingPermissionException, InvalidProhibitionSubjectTypeException {
        long userID = getSessionUserID();

        // Uses a hashtable htReachableOas of reachable oas (see find_border_oa_priv(u))
        // An oa is a key in this hashtable. The value is another hashtable that
        // represents a label of the oa. A label is a set of pairs {(op -> pcset)}, with
        // the op being the key and pcset being the value.
        // {oa -> {op -> pcset}}.
        Hashtable htReachableOas = new Hashtable();

        // BFS from u (the base node). Prepare a queue.
        HashSet<Node> visited = new HashSet<>();
        Node crtNode;

        // Get u's directly assigned attributes and put them into the queue.
        HashSet<Node> hsAttrs = getGraphMem().getParents(userID);
        ArrayList<Node> queue = new ArrayList<>(hsAttrs);

        // While the queue has elements, extract an element from the queue
        // and visit it.
        while (!queue.isEmpty()) {
            // Extract an ua from queue.
            crtNode = queue.remove(0);
            if (!visited.contains(crtNode)) {
                // If the ua has ua -> oa edges
                if (inMemUattrHasOpsets(crtNode)) {
                    // Find the set of PCs reachable from ua.
                    HashSet<Long> hsUaPcs = inMemFindPcSet(crtNode.getID(), NodeType.UA);

                    // From each discovered ua traverse the edges ua -> oa.

                    // Find the opsets of this user attribute. Note that the set of containers for this
                    // node (user attribute) may contain not only opsets.
                    HashMap<Long, HashSet<String>> assocs = getGraphMem().getSourceAssociations(crtNode.getID());

                    // Go through the containers and only for opsets do the following.
                    // For each opset ops of ua:
                    for (Long targetID : assocs.keySet()) {
                        // If oa is in htReachableOas
                        if (htReachableOas.containsKey(targetID)) {
                            // Then oa has a label op1 -> hsPcs1, op2 -> hsPcs2,...
                            // Extract its label:
                            Hashtable htOaLabel = (Hashtable)htReachableOas.get(targetID);

                            // Get the operations from the opset:
                            HashSet opers = assocs.get(targetID);
                            // For each operation in the opset
                            Iterator opersIter = opers.iterator();
                            while (opersIter.hasNext()) {
                                String sOp = (String)opersIter.next();
                                // If the oa's label already contains the operation sOp
                                if (htOaLabel.containsKey(sOp)) {
                                    // The label contains op -> some pcset.
                                    // Do the union of the old pc with ua's pcset
                                    HashSet hsPcs = (HashSet)htOaLabel.get(sOp);
                                    hsPcs.addAll(hsUaPcs);
                                } else {
                                    // The op is not in the oa's label.
                                    // Create new op -> ua's pcs mappiing in the label.
                                    HashSet hsNewPcs = new HashSet(hsUaPcs);
                                    htOaLabel.put(sOp, hsNewPcs);
                                }
                            }
                        } else {
                            // oa is not in htReachableOas.
                            // Prepare a new label
                            Hashtable htOaLabel = new Hashtable();

                            // Get the operations from the opset:
                            HashSet opers = assocs.get(targetID);
                            // For each operation in the opset
                            Iterator opersIter = opers.iterator();
                            while (opersIter.hasNext()) {
                                String sOp = (String)opersIter.next();
                                // Add op -> pcs to the label.
                                HashSet hsNewPcs = new HashSet(hsUaPcs);
                                htOaLabel.put(sOp,  hsNewPcs);
                            }

                            // Add oa -> {op -> pcs}
                            htReachableOas.put(targetID,  htOaLabel);
                        }
                    }
                }
                visited.add(crtNode);

                HashSet hsDescs = getGraphMem().getParents(crtNode.getID());
                Iterator descsIter = hsDescs.iterator();
                while (descsIter.hasNext()) {
                    Node d = (Node) descsIter.next();
                    queue.add(d);
                }
            }
        }


        // For each reachable oa in htReachableOas.keys
        for (Enumeration keys = htReachableOas.keys(); keys.hasMoreElements() ;) {
            long oaID = (long)keys.nextElement();
            // Compute {pc | oa ->+ pc}
            HashSet hsOaPcs = inMemFindPcSet(oaID, NodeType.OA);
            // Extract oa's label.
            Hashtable htOaLabel = (Hashtable)htReachableOas.get(oaID);
            // The label contains op1 -> pcs1, op2 -> pcs2,...
            // For each operation in the label
            for (Enumeration lbl = htOaLabel.keys(); lbl.hasMoreElements();) {
                String sOp = (String)lbl.nextElement();
                // Intersect the pcset corresponding to this operation,
                // which comes from the uas, with the oa's pcset.
                HashSet oaPcs = (HashSet)htOaLabel.get(sOp);
                oaPcs.retainAll(hsOaPcs);
                if (oaPcs.isEmpty()) htOaLabel.remove(sOp);
            }
        }

        return htReachableOas;
    }

    private HashSet<Long> inMemFindPcSet(Long nodeID, NodeType type) throws LoadConfigException, DatabaseException, MissingPermissionException, NodeNotFoundException, SessionDoesNotExistException, InvalidProhibitionSubjectTypeException {
        HashSet<Long> reachable = new HashSet<>();

        // Init the queue, visited
        ArrayList<Long> queue = new ArrayList<>();
        HashSet<Long> visited = new HashSet<>();

        // The current element
        Long crtNode = null;

        // Insert the start node into the queue
        queue.add(nodeID);

        // While queue is not empty
        while (!queue.isEmpty()) {
            // Extract current element from queue
            crtNode = queue.remove(0);
            // If not visited
            if (!visited.contains(crtNode)) {
                // Mark it as visited
                visited.add(crtNode);
                // Extract its direct descendants. If a descendant is an attribute,
                // insert it into the queue. If it is a pc, add it to reachable,
                // if not already there
                HashSet<Node> hsContainers = getGraphMem().getParents(crtNode);
                Iterator<Node> hsiter = hsContainers.iterator();
                while (hsiter.hasNext()) {
                    Node n = hsiter.next();
                    if(getGraphMem().getPolicies().contains(n.getID())) {
                        reachable.add(n.getID());
                    } else {
                        queue.add(n.getID());
                    }
                }
            }
        }
        return reachable;
    }

    private boolean inMemUattrHasOpsets(Node uaNode) throws LoadConfigException, DatabaseException, MissingPermissionException, NodeNotFoundException, SessionDoesNotExistException, InvalidProhibitionSubjectTypeException {
        return !getGraphMem().getSourceAssociations(uaNode.getID()).isEmpty();
    }
}
