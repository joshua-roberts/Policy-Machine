package gov.nist.csd.pm.pdp.engine;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;


import java.util.HashSet;

/**
 * Interface for making access decisions
 */
public interface Decider {
    /**
     * Check if the user has the permissions on the target node. Use '*' as the permission to check
     * if the user has any permissions on the node.
     *
     * @param userID the ID of the user.
     * @param processID the of the process.
     * @param targetID the ID of the target node.
     * @param perms the array of permission sto check for.
     * @return true if the user has the permissions on the target node, false otherwise.
     * @throws PMException if there is an error checking if the user/process has permissions on the target node.
     */
    boolean hasPermissions(long userID, long processID, long targetID, String... perms) throws PMGraphException, PMDBException;

    /**
     * List the permissions that the user has on the target node.
     *
     * @param userID the ID of the user.
     * @param processID the of the process.
     * @param targetID the ID of the target node.
     * @return the set of operations that the user is allowed to perform on the target.
     * @throws PMDBException if there is an error connecting to a database while listing the permissions.
     * @throws PMGraphException if there is an error with a node when listing the permissions.
     */
    HashSet<String> listPermissions(long userID, long processID, long targetID) throws PMDBException, PMGraphException;

    /**
     * Given a list of nodes filter out any nodes that the given user does not have the given permissions on. To filter
     * based on any permissions use Operations.ANY as the permission to check for.
     * @param userID the ID of the user.
     * @param processID the of the process.
     * @param nodes the nodes to filter from.
     * @param perms the permissions to check for.
     * @return a subset of the given nodes that the user has the given permissions on.
     */
    HashSet<NodeContext> filter(long userID, long processID, HashSet<NodeContext> nodes, String ... perms);

    /**
     * Get the children of the target node that the user has the given permissions on.
     *
     * @param userID the ID of the user.
     * @param processID the of the process.
     * @param targetID the ID of the target node.
     * @param perms the permissions the user must have on the child nodes.
     * @return the set of NGACNodes that are children of the target node and the user has the given permissions on.
     * @throws PMDBException if there is an error connecting to a database while getting the accessible children of the target node.
     * @throws PMGraphException if there is an error with a node when getting the accessible children of the target node.
     */
    HashSet<NodeContext> getChildren(long userID, long processID, long targetID, String ... perms) throws PMGraphException, PMDBException;
}
