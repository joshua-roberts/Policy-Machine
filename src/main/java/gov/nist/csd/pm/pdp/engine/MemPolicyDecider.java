package gov.nist.csd.pm.pdp.engine;

import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.NGAC;

import java.util.HashSet;

public class MemPolicyDecider extends PolicyDecider {

    public MemPolicyDecider(NGAC ngac, long userID, long processID) {
        super(ngac, userID, processID);
    }

    @Override
    public boolean hasPermissions(long targetID, String... perms) {
        return false;
    }

    @Override
    public HashSet<String> listPermissions(long targetID) {
        return null;
    }

    @Override
    public HashSet<Node> filter(HashSet<Node> nodes, String... perms) {
        return null;
    }

    @Override
    public HashSet<Node> getChildren(long targetID, String... perms) {
        return null;
    }

    @Override
    public boolean canDelete(long nodeID) {
        return false;
    }

    @Override
    public boolean canAssign(long childID, long parentID) {
        return false;
    }
}
