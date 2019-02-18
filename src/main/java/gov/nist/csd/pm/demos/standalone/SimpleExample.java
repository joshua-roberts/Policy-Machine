package gov.nist.csd.pm.demos.standalone;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.pap.graph.Graph;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.common.model.graph.nodes.NodeUtils;
import gov.nist.csd.pm.pap.graph.MemGraph;
import gov.nist.csd.pm.pdp.engine.Decider;
import gov.nist.csd.pm.pdp.engine.PReviewDecider;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertTrue;

public class SimpleExample {

    public static void main(String[] args) throws PMException {
        // 1. Create a new graph.  For this example, we'll use the in-memory implementation of the 'Graph' interface.
        Graph graph = new MemGraph();

        // 2. Create the user and object nodes.
        // create a user with name u1, ID 5, and the properties key1=value1
        long u1ID = graph.createNode(new NodeContext(5, "u1", NodeType.U, NodeUtils.toProperties("key1", "value1")));
        // create an object with name o1, ID 3, and the properties key1=value1
        long o1ID = graph.createNode(new NodeContext(3, "o1", NodeType.O, NodeUtils.toProperties("key1", "value1")));
        
        // 3. Create a user attribute 'ua1' and assign 'u1' to it.
        long ua1ID = graph.createNode(new NodeContext(4, "ua1", NodeType.UA, NodeUtils.toProperties("key1", "value1")));
        graph.assign(new NodeContext(u1ID, NodeType.U), new NodeContext(ua1ID, NodeType.UA));
        
        // 4. Create an object attribute 'oa1' and assign 'o1' to it.
        long oa1ID = graph.createNode(new NodeContext(2, "oa1", NodeType.OA, NodeUtils.toProperties("key1", "value1")));
        graph.assign(new NodeContext(o1ID, NodeType.O), new NodeContext(oa1ID, NodeType.OA));

        // 5. Create a policy class and assign the user and object attributes to it.
        long pc1ID = graph.createNode(new NodeContext(1, "pc1", NodeType.PC, NodeUtils.toProperties("key1", "value1")));
        graph.assign(new NodeContext(ua1ID, NodeType.UA), new NodeContext(pc1ID, NodeType.PC));
        graph.assign(new NodeContext(oa1ID, NodeType.OA), new NodeContext(pc1ID, NodeType.PC));

        // 6. associate 'ua1' and 'oa1' to give 'u1' read permissions on 'o1'
        graph.associate(new NodeContext(ua1ID, NodeType.UA), new NodeContext(oa1ID, NodeType.OA), new HashSet<>(Arrays.asList("read")));

        // test the configuration is correct
        // create a new policy decider with the in memory graph
        // the second parameter is a list of prohibitions, but since they aren't used in this example, null is fine
        Decider decider = new PReviewDecider(graph, null);

        // use the listPermissions method to list the permissions 'u1' has on 'o1'
        HashSet<String> permissions = decider.listPermissions(u1ID, 0, o1ID);
        assertTrue(permissions.contains("read"));
    }
}
