package gov.nist.csd.pm.demos.standalone;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.graph.Graph;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.pap.graph.MemGraph;
import gov.nist.csd.pm.pdp.engine.Decider;
import gov.nist.csd.pm.pdp.engine.PReviewDecider;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.*;
import static org.junit.Assert.assertEquals;

class BankTellerExample {

    private Random rand = new Random();

    @Test
    void TestBankTellerExample() throws PMException {
        // 1. Create a new Graph instance.  For this example, we'll use the `MemGraph` which is an in memory implementation of the Graph interface.
        Graph graph = new MemGraph();

        // 2. Create the user nodes `u1` and `u2`.
        long user1ID = graph.createNode(new NodeContext(rand.nextLong(), "u1", U, null));
        long user2ID = graph.createNode(new NodeContext(rand.nextLong(), "u2", U, null));


        // 3. Create the object, `o1` that will be the target of the access queries.
        long objectID = graph.createNode(new NodeContext(rand.nextLong(), "o1", O, null));


        // 4. Create the `RBAC` policy class node.
        long rbacID = graph.createNode(new NodeContext(rand.nextLong(), "RBAC", PC, null));


        // 5. Create an object attribute for the `Accounts`.
        long accountsID = graph.createNode(new NodeContext(rand.nextLong(), "Accounts", OA, null));


        // 6. Create the `Teller` and `Auditor` user attributes.
        long tellerID = graph.createNode(new NodeContext(rand.nextLong(), "Teller", UA, null));
        long auditorID = graph.createNode(new NodeContext(rand.nextLong(), "Auditor", UA, null));


        // 7. Assign the `Accounts` object attribute to the `RBAC` policy class node.
        graph.assign(new NodeContext(accountsID, OA), new NodeContext(rbacID, PC));


        // 8. Assign the object, `o1`, to the `Accounts` object attribute.
        graph.assign(new NodeContext(objectID, O), new NodeContext(accountsID, OA));


        // 9. Assign `u1` to the `Teller` user attribute and `u2` to the `Auditor` user attribute.
        graph.assign(new NodeContext(user1ID, U), new NodeContext(tellerID, UA));
        graph.assign(new NodeContext(user2ID, U), new NodeContext(auditorID, UA));


        // 10. Create the associations for `Teller` and `Auditor` on `Account` in RBAC. `Teller` has read and write permissions, while `Auditor` just has read permissions.
        graph.associate(new NodeContext(tellerID, UA), new NodeContext(accountsID, OA), new HashSet<>(Arrays.asList("r", "w")));
        graph.associate(new NodeContext(auditorID, UA), new NodeContext(accountsID, OA), new HashSet<>(Arrays.asList("r")));


        // 11. Create the `Branches` policy class.
        long branchesID = graph.createNode(new NodeContext(rand.nextLong(), "branches", PC, null));


        // 12. Create an object attribute for `Branch 1`.
        long branch1OAID = graph.createNode(new NodeContext(rand.nextLong(), "branch 1", OA, null));

        // 13. Assign the branch 1 OA to the branches PC
        //graph.assign(new NodeContext(branch1OAID, OA), new NodeContext(branchesID, PC));

        // 14. Create the `Branch 1` user attribute
        long branches1UAID = graph.createNode(new NodeContext(rand.nextLong(), "branch 1", UA, null));


        // 15. Assign the object, `o1`, to the `Branch 1` object attribute
        graph.assign(new NodeContext(objectID, O), new NodeContext(branch1OAID, OA));


        // 16. Assign the users, `u1` and `u2`, to the branch 1 user attribute
        graph.assign(new NodeContext(user1ID, U), new NodeContext(branches1UAID, UA));
        graph.assign(new NodeContext(user2ID, U), new NodeContext(branches1UAID, UA));


        // 17. Create an association between the `branch 1` user attribute and the `branch 1` object attribute.
        //This will give both users read and write on `o1` under the `branches` policy class.
        graph.associate(new NodeContext(branches1UAID, UA), new NodeContext(branch1OAID, OA), new HashSet<>(Arrays.asList("r", "w")));


        // 18. Test the configuration using the `PReviewDecider` implementation of the `Decider` interface.
        //The constructor for a `PReviewDecider` receives the graph we created and a list of prohibitions.
        //Since no prohibitions are used in this example, we'll pass null.
        Decider decider = new PReviewDecider(graph, null);


        // 19. Check that `u1` has read and write permissions on `o1`.
        HashSet<String> permissions = decider.listPermissions(user1ID, 0, objectID);
        assertEquals(permissions, new HashSet<>(Arrays.asList("r", "w")));


        // 20. Check that `u1` has read permissions on `o1`.
        permissions = decider.listPermissions(user2ID, 0, objectID);
        assertEquals(permissions, new HashSet<>(Arrays.asList("r")));
    }
}
