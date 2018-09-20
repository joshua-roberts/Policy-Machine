package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.model.graph.Association;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.graph.NodeType;
import gov.nist.csd.pm.model.prohibitions.ProhibitionSubjectType;
import gov.nist.csd.pm.pdp.analytics.PmAnalyticsEntry;
import gov.nist.csd.pm.model.exceptions.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.csd.pm.model.Constants.ALL_OPERATIONS;
import static gov.nist.csd.pm.model.Constants.ANY_OPERATIONS;

public class AnalyticsService extends Service{

    public List<PmAnalyticsEntry> getUsersPermissionsOn(long targetID)
            throws NodeNotFoundException, ConfigurationException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        //check that the target node exists
        Node target = getGraph().getNode(targetID);
        if(target == null){
            throw new NodeNotFoundException(targetID);
        }

        return getAnalytics().getUsersWithAccessOn(target);
    }

    public PmAnalyticsEntry getUserPermissionsOn(long targetID, long userID)
            throws NodeNotFoundException, NoSubjectParameterException, InvalidProhibitionSubjectTypeException, ConfigurationException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        //check that the target and user nodes exist
        Node target = getGraph().getNode(targetID);
        if(target == null){
            throw new NodeNotFoundException(targetID);
        }
        Node user = getGraph().getNode(userID);
        if(user == null){
            throw new NodeNotFoundException(userID);
        }

        //get permitted ops for the subject on the target
        PmAnalyticsEntry userAccess = getAnalytics().getUserAccessOn(user, target);

        //get prohibited ops for the subject on the target
        HashSet<String> prohibitedOps = getProhibitedOps(target.getID(), user.getID(), ProhibitionSubjectType.U.toString());

        //remove prohibited ops
        userAccess.getOperations().removeAll(prohibitedOps);

        return userAccess;
    }

    public List<PmAnalyticsEntry> getAccessibleChildren(long targetID, long userID) throws NodeNotFoundException, NoUserParameterException, ConfigurationException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        //check that a user id is present
        if(userID == 0){
            throw new NoUserParameterException();
        }

        //check that the user and target nodes exist
        Node target = getGraph().getNode(targetID);
        if(target == null){
            throw new NodeNotFoundException(targetID);
        }
        Node user = getGraph().getNode(userID);
        if(user == null){
            throw new NodeNotFoundException(userID);
        }

        return getAnalytics().getAccessibleChildrenOf(target, user);
    }

    public List<PmAnalyticsEntry> getAccessibleNodes(long userID) throws NodeNotFoundException, NoUserParameterException, ConfigurationException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        //check that the user id is present
        if(userID == 0){
            throw new NoUserParameterException();
        }

        //check that the user exists
        Node user = getGraph().getNode(userID);
        if(user == null){
            throw new NodeNotFoundException(userID);
        }

        //get the accessible nodes and add it to the cache

        return getAnalytics().getAccessibleNodes(user);
    }

    public List<PmAnalyticsEntry> getAccessibleNodes(Node user) throws ConfigurationException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        //get the accessible nodes and add it to the cache

        return getAnalytics().getAccessibleNodes(user);
    }

    public HashSet<String> getProhibitedOps(long targetID, long subjectID, String subjectType) throws NodeNotFoundException, NoSubjectParameterException, InvalidProhibitionSubjectTypeException, ConfigurationException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        if(subjectID == 0){
            throw new NoSubjectParameterException();
        }

        //check if the subject type is valid
        ProhibitionSubjectType type = ProhibitionSubjectType.toProhibitionSubjectType(subjectType);

        //check that the user and target nodes exist
        Node target = getGraph().getNode(targetID);
        if(target == null){
            throw new NodeNotFoundException(targetID);
        }
        if(type.equals(ProhibitionSubjectType.U)) {
            Node user = getGraph().getNode(subjectID);
            if (user == null) {
                throw new NodeNotFoundException(subjectID);
            }
        }

        return getAnalytics().getProhibitedOps(targetID, subjectID);
    }

    public void checkPermissions(Node user, long process, long targetID, String reqPerm) throws MissingPermissionException, NoSubjectParameterException, NodeNotFoundException, InvalidProhibitionSubjectTypeException, ConfigurationException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        Node node = getGraph().getNode(targetID);
        if(node == null) {
            throw new NodeNotFoundException(targetID);
        }

        //only check if node is OA or O
        if(node.getType().equals(NodeType.OA) || node.getType().equals(NodeType.O)) {
            if (user == null && process == 0) {
                throw new MissingPermissionException("No User or Process found in checking permissions");
            }

            if (user != null) {
                PmAnalyticsEntry perms = getUserPermissionsOn(targetID, user.getID());
                HashSet<String> operations = perms.getOperations();

                if (!perms.getOperations().contains(reqPerm)
                        && !perms.getOperations().contains(ALL_OPERATIONS)
                        && !(reqPerm.equals(ANY_OPERATIONS) && !operations.isEmpty())) {
                    throw new MissingPermissionException("User " + user.getName() + " does not have the following permission on " + node.getName() + ": " + reqPerm);
                }
            }

            if (process != 0) {
                HashSet<String> prohibitedOps = getProhibitedOps(targetID, process, ProhibitionSubjectType.P.toString());
                if (prohibitedOps.contains(reqPerm)) {
                    throw new MissingPermissionException("The current process does not have the following permission on " + node.getName() + ": " + reqPerm);
                }
            }
        }
    }

    public HashSet<Node> inMemFindPcSet(Node node, NodeType type) throws ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        HashSet<Node> reachable = new HashSet<>();

        // Init the queue, visited
        ArrayList<Node> queue = new ArrayList<>();
        HashSet<Node> visited = new HashSet<>();

        // The current element
        Node crtNode = null;

        // Insert the start node into the queue
        queue.add(node);

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
                HashSet<Node> hsContainers = getGraph().getParents(crtNode);
                Iterator<Node> hsiter = hsContainers.iterator();
                while (hsiter.hasNext()) {
                    Node n = hsiter.next();
                    if (n.getType().equals(type)) {
                        queue.add(n);
                    } else if (n.getType().equals(NodeType.PC)) {
                        reachable.add(n);
                    }
                }
            }
        }
        return reachable;
    }

    public boolean inMemUattrHasOpsets(Node uaNode) throws ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        return !getGraph().getUattrAssociations(uaNode.getID()).isEmpty();
    }

    public HashSet getPos(String session) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException {
        // Prepare the hashset to return.
        HashSet hsOa = new HashSet();

        // Call find_border_oa_priv(u). The result is a Hashtable
        // htoa = {oa -> {op -> pcset}}:
        Hashtable htOa = findBorderOaPrivRestrictedInternal(session);

        // For each returned oa (key in htOa)
        for (Enumeration oas = htOa.keys(); oas.hasMoreElements(); ) {
            long oaID = (long)oas.nextElement();
            Node node = getGraph().getNode(oaID);

            // Compute oa's required PCs by calling find_pc_set(sOaID).
            HashSet hsReqPcs = inMemFindPcSet(node, NodeType.OA);
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
                    hsOa.add(node);
                    break;
                }
            }
        }
        return hsOa;
    }

    public Hashtable findBorderOaPrivRestrictedInternal(String session) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException {
        Node sessionUser = getSessionUser(session);
        // Uses a hashtable htReachableOas of reachable oas (see find_border_oa_priv(u))
        // An oa is a key in this hashtable. The value is another hashtable that
        // represents a label of the oa. A label is a set of pairs {(op -> pcset)}, with
        // the op being the key and pcset being the value.
        // {oa -> {op -> pcset}}.
        Hashtable htReachableOas = new Hashtable();

        // BFS from u (the base node). Prepare a queue.
        ArrayList<Node> queue = new ArrayList<>();
        HashSet<Node> visited = new HashSet<>();
        Node crtNode;

        // Get u's directly assigned attributes and put them into the queue.
        HashSet<Node> hsAttrs = getGraph().getParents(sessionUser);
        queue.addAll(hsAttrs);

        // While the queue has elements, extract an element from the queue
        // and visit it.
        while (!queue.isEmpty()) {
            // Extract an ua from queue.
            crtNode = queue.remove(0);
            if (!visited.contains(crtNode)) {
                // If the ua has ua -> oa edges
                if (inMemUattrHasOpsets(crtNode)) {
                    // Find the set of PCs reachable from ua.
                    HashSet<Node> hsUaPcs = inMemFindPcSet(crtNode, NodeType.UA);

                    // From each discovered ua traverse the edges ua -> oa.

                    // Find the opsets of this user attribute. Note that the set of containers for this
                    // node (user attribute) may contain not only opsets.
                    List<Association> assocs = getGraph().getUattrAssociations(crtNode.getID());

                    // Go through the containers and only for opsets do the following.
                    // For each opset ops of ua:
                    Iterator<Association> opsetsIter = assocs.iterator();
                    while (opsetsIter.hasNext()) {
                        Association assoc = opsetsIter.next();
                        // If this is an opset
                        // Find the object attributes of this opset.
                        Node oattr = assoc.getParent();
                        // If oa is in htReachableOas
                        if (htReachableOas.containsKey(oattr.getID())) {
                            // Then oa has a label op1 -> hsPcs1, op2 -> hsPcs2,...
                            // Extract its label:
                            Hashtable htOaLabel = (Hashtable)htReachableOas.get(oattr.getID());

                            // Get the operations from the opset:
                            HashSet opers = assoc.getOps();
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
                            HashSet opers = assoc.getOps();
                            // For each operation in the opset
                            Iterator opersIter = opers.iterator();
                            while (opersIter.hasNext()) {
                                String sOp = (String)opersIter.next();
                                // Add op -> pcs to the label.
                                HashSet hsNewPcs = new HashSet(hsUaPcs);
                                htOaLabel.put(sOp,  hsNewPcs);
                            }

                            // Add oa -> {op -> pcs}
                            htReachableOas.put(oattr.getID(),  htOaLabel);
                        }
                    }
                }
                visited.add(crtNode);

                HashSet hsDescs = getGraph().getParents(crtNode);
                Iterator descsIter = hsDescs.iterator();
                while (descsIter.hasNext()) {
                    Node d = (Node) descsIter.next();
                    if (d.getType().equals(NodeType.UA)) {
                        queue.add(d);
                    }
                }
            }
        }


        // For each reachable oa in htReachableOas.keys
        for (Enumeration keys = htReachableOas.keys(); keys.hasMoreElements() ;) {
            long sOaID = (long)keys.nextElement();
            // Compute {pc | oa ->+ pc}
            HashSet hsOaPcs = inMemFindPcSet(getGraph().getNode(sOaID), NodeType.OA);
            // Extract oa's label.
            Hashtable htOaLabel = (Hashtable)htReachableOas.get(sOaID);
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
}
