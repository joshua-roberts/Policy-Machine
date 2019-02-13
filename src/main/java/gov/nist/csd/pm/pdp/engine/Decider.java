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
     * @param userID THe ID of the user.
     * @param processID THe of the process.
     * @param targetID The ID of the target node.
     * @param perms The array of permission sto check for.
     * @return True if the user has the permissions on the target node, false otherwise.
     * @throws PMException If there is an error checking if the user/process has permissions on the target node.
     */
    boolean hasPermissions(long userID, long processID, long targetID, String... perms) throws PMException;

    /**
     * List the permissions that the user has on the target node.
     *
     * @param userID THe ID of the user.
     * @param processID THe of the process.
     * @param targetID The ID of the target node.
     * @return The set of operations that the user is allowed to perform on the target.
     * @throws PMException If there is an error listing the permissions the user/process has on the target node.
     */
    HashSet<String> listPermissions(long userID, long processID, long targetID) throws PMException;

    /**
     * Given a list of nodes filter out any nodes that the given user does not have the given permissions on. To filter
     * based on any permissions use Operations.ANY as the permission to check for.
     * @param userID THe ID of the user.
     * @param processID THe of the process.
     * @param nodes The nodes to filter from.
     * @param perms The permissions to check for.
     * @return A subset of the given nodes that the user has the given permissions on.
     * @throws PMException If there is an error filtering the provided list.
     */
    HashSet<NodeContext> filter(long userID, long processID, HashSet<NodeContext> nodes, String ... perms) throws PMException;

    /**
     * Get the children of the target node that the user has the given permissions on.
     *
     * @param userID THe ID of the user.
     * @param processID THe of the process.
     * @param targetID The ID of the target node.
     * @param perms The permissions the user must have on the child nodes.
     * @return The set of NGACNodes that are children of the target node and the user has the given permissions on.
     * @throws PMException If there is an error getting the accessible children of the target node.
     */
    HashSet<NodeContext> getChildren(long userID, long processID, long targetID, String ... perms) throws PMException;
}
