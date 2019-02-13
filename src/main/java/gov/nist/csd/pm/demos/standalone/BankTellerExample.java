package gov.nist.csd.pm.demos.standalone;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.graph.MemGraph;
import gov.nist.csd.pm.pap.graph.Neo4jGraph;
import gov.nist.csd.pm.pdp.engine.Decider;
import gov.nist.csd.pm.pdp.engine.PReviewDecider;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.*;
import static org.junit.Assert.assertTrue;

public class BankTellerExample {

    private Random rand = new Random();

    @Test
    public void TestBankTellerExample() throws PMException {
        Graph graph = new MemGraph();

        // users
        long user1ID = graph.createNode(new NodeContext(rand.nextLong(), "u1", U, null));
        long user2ID = graph.createNode(new NodeContext(rand.nextLong(), "u2", U, null));

        // objects
        long objectID = graph.createNode(new NodeContext(rand.nextLong(), "o1", O, null));

        // create the RBAC pc
        long rbacID = graph.createNode(new NodeContext(rand.nextLong(), "RBAC", PC, null));

        // object attributes
        long accountsID = graph.createNode(new NodeContext(rand.nextLong(), "Accounts", OA, null));

        // user attributes
        long tellerID = graph.createNode(new NodeContext(rand.nextLong(), "Teller", UA, null));
        long auditorID = graph.createNode(new NodeContext(rand.nextLong(), "Auditor", UA, null));

        // assignments
        // assign the object attribute to the policy class
        graph.assign(new NodeContext(accountsID, OA), new NodeContext(rbacID, PC));

        // assign the object to accounts
        graph.assign(new NodeContext(objectID, O), new NodeContext(accountsID, OA));

        // assign the user attributes to the policy class
        graph.assign(new NodeContext(user1ID, U), new NodeContext(tellerID, UA));
        graph.assign(new NodeContext(user2ID, U), new NodeContext(auditorID, UA));

        graph.associate(new NodeContext(tellerID, UA), new NodeContext(accountsID, OA), new HashSet<>(Arrays.asList("r", "w")));
        graph.associate(new NodeContext(auditorID, UA), new NodeContext(accountsID, OA), new HashSet<>(Arrays.asList("r")));

        // create the branches pc
        long branchesID = graph.createNode(new NodeContext(rand.nextLong(), "branches", PC, null));

        // create the branch1 object attribute
        long branches1OAID = graph.createNode(new NodeContext(rand.nextLong(), "branch 1", OA, null));

        // create the branch1 user attribute
        long branches1UAID = graph.createNode(new NodeContext(rand.nextLong(), "branch 1", UA, null));

        // assignments
        // assign the object to the branches 1 object attribute
        graph.assign(new NodeContext(objectID, O), new NodeContext(branches1OAID, OA));

        // assign the users to the branch 1 user attribute
        graph.assign(new NodeContext(user1ID, U), new NodeContext(branches1UAID, UA));
        graph.assign(new NodeContext(user2ID, U), new NodeContext(branches1UAID, UA));

        // associate the branch 1 user attribute to the branch 1 object attribute
        graph.associate(new NodeContext(branches1UAID, UA), new NodeContext(branches1OAID, OA), new HashSet<>(Arrays.asList("r", "w")));

        // assign the object attributes to the policy class
        graph.assign(new NodeContext(branches1OAID, OA), new NodeContext(branchesID, PC));

        // test the configuration
        Decider decider = new PReviewDecider(graph, null);

        // user: u1
        // target: 'o1'
        // expected: [r, w]
        // actual: [r, w]
        HashSet<String> permissions = decider.listPermissions(user1ID, 0, objectID);
        assertTrue(permissions.contains("r"));
        assertTrue(permissions.contains("w"));

        // user: u2
        // target: 'o1'
        // expected: [r]
        // actual: [r]
        permissions = decider.listPermissions(user2ID, 0, objectID);
        assertTrue(permissions.contains("r"));
    }
}
