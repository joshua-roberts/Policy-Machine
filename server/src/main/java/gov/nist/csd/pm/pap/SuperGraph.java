package gov.nist.csd.pm.pap;

import gov.nist.csd.pm.common.exceptions.PMGraphException;
import gov.nist.csd.pm.common.util.NodeUtils;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.Graph;
import gov.nist.csd.pm.graph.MemGraph;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.graph.model.nodes.NodeType;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static gov.nist.csd.pm.common.constants.Operations.ALL_OPERATIONS;
import static gov.nist.csd.pm.common.constants.Properties.*;
import static gov.nist.csd.pm.common.util.NodeUtils.generatePasswordHash;
import static gov.nist.csd.pm.graph.model.nodes.NodeType.UA;

public class SuperGraph {

    private static Node superPC, superPCRep, superUA1, superUA2, superU, superOA, superO;

    public static void check(Graph graph) throws PMException {
        Random rand = new Random();

        HashMap<String, String> props = NodeUtils.toProperties(NAMESPACE_PROPERTY, "super");

        Set<Node> nodes = graph.search("super_ua1", UA.toString(), props);
        if(nodes.isEmpty()) {
            superUA1 = graph.createNode(rand.nextLong(), "super_ua1", UA, props);
        } else {
            superUA1 = nodes.iterator().next();
        }
        nodes = graph.search("super_ua2", UA.toString(), props);
        if(nodes.isEmpty()) {
            superUA2 = graph.createNode(rand.nextLong(), "super_ua2", UA, props);
        } else {
            superUA2 = nodes.iterator().next();
        }
        nodes = graph.search("super", NodeType.U.toString(), props);
        if(nodes.isEmpty()) {
            try {
                props.put(PASSWORD_PROPERTY, generatePasswordHash("super"));
            }
            catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new PMGraphException(e.getMessage());
            }
            superU = graph.createNode(rand.nextLong(), "super", NodeType.U, props);
            props.remove(PASSWORD_PROPERTY);
        } else {
            superU = nodes.iterator().next();
        }

        nodes = graph.search("super", NodeType.OA.toString(), props);
        if(nodes.isEmpty()) {
            superOA = graph.createNode(rand.nextLong(), "super", NodeType.OA, props);
        } else {
            superOA = nodes.iterator().next();
        }
        nodes = graph.search("super", NodeType.O.toString(), props);
        if(nodes.isEmpty()) {
            superO = graph.createNode(rand.nextLong(), "super", NodeType.O, props);
        } else {
            superO = nodes.iterator().next();
        }
        nodes = graph.search("super rep", NodeType.OA.toString(), props);
        if(nodes.isEmpty()) {
            superPCRep = graph.createNode(rand.nextLong(), "super rep", NodeType.OA, props);
        } else {
            superPCRep = nodes.iterator().next();
        }
        nodes = graph.search("super", NodeType.PC.toString(), props);
        if(nodes.isEmpty()) {
            // add the rep oa ID to the properties
            props.put(REP_PROPERTY, String.valueOf(superPCRep.getID()));
            superPC = graph.createNode(rand.nextLong(), "super", NodeType.PC, props);
            props.remove(REP_PROPERTY);
        } else {
            superPC = nodes.iterator().next();

            // make sure the rep ID property is correct
            // if the rep ID for the pc node is null, empty, or doesn't match the current rep ID, update the ID
            String repID = superPC.getProperties().get(REP_PROPERTY);
            if(repID == null || repID.isEmpty() || !repID.equals(String.valueOf(superPCRep.getID()))) {
                repID = String.valueOf(superPCRep.getID());
                superPC.getProperties().put(REP_PROPERTY, repID);
                // update the node
                graph.updateNode(superPC.getID(), null, superPC.getProperties());
            }
        }

        // check super ua1 is assigned to super pc
        Set<Long> children = graph.getChildren(superPC.getID());
        if(!children.contains(superUA1.getID())) {
            graph.assign(superUA1.getID(), superPC.getID());
        }
        // check super ua2 is assigned to super pc
        children = graph.getChildren(superPC.getID());
        if(!children.contains(superUA2.getID())) {
            graph.assign(superUA2.getID(), superPC.getID());
        }
        // check super user is assigned to super ua1
        children = graph.getChildren(superUA1.getID());
        if(!children.contains(superU.getID())) {
            graph.assign(superU.getID(), superUA1.getID());
        }
        // check super user is assigned to super ua2
        children = graph.getChildren(superUA2.getID());
        if(!children.contains(superU.getID())) {
            graph.assign(superU.getID(),superUA2.getID());
        }
        // check super oa is assigned to super pc
        children = graph.getChildren(superPC.getID());
        if(!children.contains(superOA.getID())) {
            graph.assign(superOA.getID(), superPC.getID());
        }
        // check that the super rep is assigned to super oa
        children = graph.getChildren(superOA.getID());
        if(!children.contains(superPCRep.getID())) {
            graph.assign(superPCRep.getID(), superOA.getID());
        }
        // check super o is assigned to super oa
        if(!children.contains(superO.getID())) {
            graph.assign(superO.getID(), superOA.getID());
        }

        // associate super ua to super oa
        graph.associate(superUA2.getID(), superOA.getID(), new HashSet<>(Arrays.asList(ALL_OPERATIONS)));
        graph.associate(superUA1.getID(), superUA2.getID(), new HashSet<>(Arrays.asList(ALL_OPERATIONS)));
    }

    public static Node getSuperPC() {
        return superPC;
    }

    public static Node getSuperPCRep() {
        return superPCRep;
    }

    public static Node getSuperUA1() {
        return superUA1;
    }

    public static Node getSuperUA2() {
        return superUA2;
    }

    public static Node getSuperU() {
        return superU;
    }

    public static Node getSuperOA() {
        return superOA;
    }

    public static Node getSuperO() {
        return superO;
    }
}
