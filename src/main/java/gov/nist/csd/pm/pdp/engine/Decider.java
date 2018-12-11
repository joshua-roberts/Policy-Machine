package gov.nist.csd.pm.pdp.engine;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.nodes.Node;


import java.util.HashSet;

/**
 * Interface for making access decisions
 */
public interface Decider {
    /**
     * Check if the user has the permissions on the target node. Use '*' as the permission to check
     * if the user has any permissions on the node.
     * @param targetID The ID of the target node.
     * @param perms The array of permission sto check for.
     * @return True if the user has the permissions on the target node, false otherwise.
     */
    boolean hasPermissions(long userID, long processID, long targetID, String... perms) throws SessionDoesNotExistException, LoadConfigException, MissingPermissionException, DatabaseException, NodeNotFoundException, InvalidProhibitionSubjectTypeException;

    /**
     * List the permissions that the user has on the target node.
     * @param targetID The ID of the target node.
     * @return The set of operations that the user is allowed to perform on the target.
     */
    HashSet<String> listPermissions(long userID, long processID, long targetID) throws DatabaseException, LoadConfigException, SessionDoesNotExistException, MissingPermissionException, NodeNotFoundException, InvalidProhibitionSubjectTypeException;

    /**
     * Given a list of nodes filter out any nodes that the given user does not have the given permissions on.
     * Filter based on any permissions using '*' as the permission to check for.
     * @param nodes The nodes to filter from.
     * @param perms The permissions to check for.
     * @return A subset of the given nodes that the user has the given permissions on.
     */
    HashSet<Node> filter(long userID, long processID, HashSet<Node> nodes, String ... perms) throws SessionDoesNotExistException, LoadConfigException, MissingPermissionException, DatabaseException, NodeNotFoundException;

    /**
     * Get the children of the target node that the user has the given permissions on.
     * @param targetID The ID of the target node.
     * @param perms The permissions the user must have on the child nodes.
     * @return The set of NGACNodes that are children of the target node and the user has the given permissions on.
     */
    HashSet<Node> getChildren(long userID, long processID, long targetID, String ... perms) throws SessionDoesNotExistException, LoadConfigException, DatabaseException, MissingPermissionException, NodeNotFoundException, InvalidProhibitionSubjectTypeException;
}
