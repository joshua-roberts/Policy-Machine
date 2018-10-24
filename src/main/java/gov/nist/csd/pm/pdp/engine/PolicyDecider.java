package gov.nist.csd.pm.pdp.engine;

import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.NGAC;

import java.util.HashSet;

public abstract class PolicyDecider {

    private NGAC ngac;
    private long userID;
    private long processID;

    /**
     * Create a new PolicyDecider with with the given NGAC graph, user ID, and process ID.
     * @param ngac The NGAC Graph to use in the policy decision.
     * @param userID The ID of the user to make a policy decision for.
     * @param processID the ID of the process to make a policy decision for.
     */
    public PolicyDecider(NGAC ngac, long userID, long processID) throws IllegalArgumentException {
        if(userID == 0) {
            throw new IllegalArgumentException("user ID cannot be 0");
        }

        this.ngac = ngac;
        this.userID = userID;
        this.processID = processID;
    }

    /**
     * Check if the user has the permissions on the target node. Use '*' as the permission to check
     * if the user has any permissions on the node.
     * @param targetID The ID of the target node.
     * @param perms The array of permission sto check for.
     * @return True if the user has the permissions on the target node, false otherwise.
     */
    public abstract boolean hasPermissions(long targetID, String... perms);

    /**
     * List the permissions that the user has on the target node.
     * @param targetID The ID of the target node.
     * @return The set of operations that the user is allowed to perform on the target.
     */
    public abstract HashSet<String> listPermissions(long targetID);

    /**
     * Given a list of nodes filter out any nodes that the given user does not have the given permissions on.
     * Filter based on any permissions using '*' as the permission to check for.
     * @param nodes The nodes to filter from.
     * @param perms The permissions to check for.
     * @return A subset of the given nodes that the user has the given permissions on.
     */
    public abstract HashSet<Node> filter(HashSet<Node> nodes, String ... perms);

    /**
     * Get the children of the target node that the user has the given permissions on.
     * @param targetID The ID of the target node.
     * @param perms The permissions the user must have on the child nodes.
     * @return The set of NGACNodes that are children of the target node and the user has the given permissions on.
     */
    public abstract HashSet<Node> getChildren(long targetID, String ... perms);

    /**
     * Check that the current user is allowed to delete the given node.
     * @param nodeID the ID of the node to check if the user an delete.
     * @return True if the user can delete the node, false otherwise.
     */
    public abstract boolean canDelete(long nodeID);

    /**
     * Checks that the current User is allowed to assign the child node to the parent node.
     * @param childID The ID of the child node.
     * @param parentID The ID of the parent node.
     * @return True if the current user is allowed to make this assignment, false otherwise.
     */
    public abstract boolean canAssign(long childID, long parentID);
}
