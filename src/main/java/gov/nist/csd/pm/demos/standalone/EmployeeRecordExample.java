package gov.nist.csd.pm.demos.standalone;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.pap.graph.Graph;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.pap.graph.MemGraph;
import gov.nist.csd.pm.pdp.engine.Decider;
import gov.nist.csd.pm.pdp.engine.PReviewDecider;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.*;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

class EmployeeRecordExample {

    private Random rand = new Random();

    @Test
    void TestEmployeeRecordExample() throws PMException {
        Graph graph = new MemGraph();

        // create nodes
        // object attributes
        long salariesID = graph.createNode(new NodeContext(rand.nextLong(), "Salaries", OA, null));
        long ssnsID = graph.createNode(new NodeContext(rand.nextLong(), "SSNs", OA, null));
        long grp1SalariesID = graph.createNode(new NodeContext(rand.nextLong(), "Grp1 Salaries", OA, null));
        long grp2SalariesID = graph.createNode(new NodeContext(rand.nextLong(), "Grp2 Salaries", OA, null));
        long publicID = graph.createNode(new NodeContext(rand.nextLong(), "Public Info", OA, null));
        
        long bobRecID = graph.createNode(new NodeContext(rand.nextLong(), "Bob Record", OA, null));
        long bobRID = graph.createNode(new NodeContext(rand.nextLong(), "Bob r", OA, null));
        long bobRWID = graph.createNode(new NodeContext(rand.nextLong(), "Bob r/w", OA, null));
        
        long aliceRecID = graph.createNode(new NodeContext(rand.nextLong(), "Alice Record", OA, null));
        long aliceRID = graph.createNode(new NodeContext(rand.nextLong(), "Alice r", OA, null));
        long aliceRWID = graph.createNode(new NodeContext(rand.nextLong(), "Alice r/w", OA, null));

        // objects for bob's name, salary, and ssn
        long bobNameID = graph.createNode(new NodeContext(rand.nextLong(), "bob name", O, null));
        long bobSalaryID = graph.createNode(new NodeContext(rand.nextLong(), "bob salary", O, null));
        long bobSSNID = graph.createNode(new NodeContext(rand.nextLong(), "bob ssn", O, null));

        // objects for alice's name, salary, and ssn
        long aliceNameID = graph.createNode(new NodeContext(rand.nextLong(), "alice name", O, null));
        long aliceSalaryID = graph.createNode(new NodeContext(rand.nextLong(), "alice salary", O, null));
        long aliceSSNID = graph.createNode(new NodeContext(rand.nextLong(), "alice ssn", O, null));

        // user attributes
        long hrID = graph.createNode(new NodeContext(rand.nextLong(), "HR", UA, null));
        long grp1MgrID = graph.createNode(new NodeContext(rand.nextLong(), "Grp1Mgr", UA, null));
        long grp2MgrID = graph.createNode(new NodeContext(rand.nextLong(), "Grp2Mgr", UA, null));
        long staffID = graph.createNode(new NodeContext(rand.nextLong(), "Staff", UA, null));
        long bobUAID = graph.createNode(new NodeContext(rand.nextLong(), "Bob", UA, null));
        long aliceUAID = graph.createNode(new NodeContext(rand.nextLong(), "Alice", UA, null));

        // users
        long bobID = graph.createNode(new NodeContext(rand.nextLong(), "bob", U, null));
        long aliceID = graph.createNode(new NodeContext(rand.nextLong(), "alice", U, null));
        long charlieID = graph.createNode(new NodeContext(rand.nextLong(), "charlie", U, null));

        // policy class
        long pcID = graph.createNode(new NodeContext(rand.nextLong(), "Employee Records", PC, null));

        // assignments
        // assign users to user attributes
        graph.assign(new NodeContext(charlieID, U), new NodeContext(hrID, UA));
        graph.assign(new NodeContext(bobID, U), new NodeContext(grp1MgrID, UA));
        graph.assign(new NodeContext(aliceID, U), new NodeContext(grp2MgrID, UA));
        graph.assign(new NodeContext(charlieID, U), new NodeContext(staffID, UA));
        graph.assign(new NodeContext(bobID, U), new NodeContext(staffID, UA));
        graph.assign(new NodeContext(aliceID, U), new NodeContext(staffID, UA));
        graph.assign(new NodeContext(bobID, U), new NodeContext(bobUAID, UA));
        graph.assign(new NodeContext(aliceID, U), new NodeContext(aliceUAID, UA));

        // assign objects to object attributes
        // salary objects
        graph.assign(new NodeContext(bobSalaryID, O), new NodeContext(salariesID, OA));
        graph.assign(new NodeContext(bobSalaryID, O), new NodeContext(grp1SalariesID, OA));
        graph.assign(new NodeContext(bobSalaryID, O), new NodeContext(bobRID, OA));

        graph.assign(new NodeContext(aliceSalaryID, O), new NodeContext(salariesID, OA));
        graph.assign(new NodeContext(aliceSalaryID, O), new NodeContext(grp2SalariesID, OA));
        graph.assign(new NodeContext(aliceSalaryID, O), new NodeContext(aliceRID, OA));

        // ssn objects
        graph.assign(new NodeContext(bobSSNID, O), new NodeContext(ssnsID, OA));
        graph.assign(new NodeContext(bobSSNID, O), new NodeContext(bobRWID, OA));

        graph.assign(new NodeContext(aliceSSNID, O), new NodeContext(ssnsID, OA));
        graph.assign(new NodeContext(aliceSSNID, O), new NodeContext(aliceRWID, OA));

        // name objects
        graph.assign(new NodeContext(bobNameID, O), new NodeContext(publicID, OA));
        graph.assign(new NodeContext(bobNameID, O), new NodeContext(bobRWID, OA));

        graph.assign(new NodeContext(aliceNameID, O), new NodeContext(publicID, OA));
        graph.assign(new NodeContext(aliceNameID, O), new NodeContext(aliceRWID, OA));
        
        // bob and alice r/w containers to their records
        graph.assign(new NodeContext(bobRID, OA), new NodeContext(bobRecID, OA));
        graph.assign(new NodeContext(bobRWID, OA), new NodeContext(bobRecID, OA));
        
        graph.assign(new NodeContext(aliceRID, OA), new NodeContext(aliceRecID, OA));
        graph.assign(new NodeContext(aliceRWID, OA), new NodeContext(aliceRecID, OA));


        // assign object attributes to policy classes
        graph.assign(new NodeContext(salariesID, OA), new NodeContext(pcID, PC));
        graph.assign(new NodeContext(ssnsID, OA), new NodeContext(pcID, PC));
        graph.assign(new NodeContext(grp1SalariesID, OA), new NodeContext(pcID, PC));
        graph.assign(new NodeContext(grp2SalariesID, OA), new NodeContext(pcID, PC));
        graph.assign(new NodeContext(publicID, OA), new NodeContext(pcID, PC));
        graph.assign(new NodeContext(bobRecID, OA), new NodeContext(pcID, PC));
        graph.assign(new NodeContext(aliceRecID, OA), new NodeContext(pcID, PC));
        
        // associations
        HashSet<String> rw = new HashSet<>(Arrays.asList("r", "w"));
        HashSet<String> r = new HashSet<>(Arrays.asList("r"));
        
        graph.associate(new NodeContext(hrID, UA), new NodeContext(salariesID, OA), rw);
        graph.associate(new NodeContext(hrID, UA), new NodeContext(ssnsID, OA), rw);
        graph.associate(new NodeContext(grp1MgrID, UA), new NodeContext(grp1SalariesID, OA), r);
        graph.associate(new NodeContext(grp2MgrID, UA), new NodeContext(grp2SalariesID, OA), r);
        graph.associate(new NodeContext(staffID, UA), new NodeContext(publicID, OA), r);
        graph.associate(new NodeContext(bobUAID, UA), new NodeContext(bobRWID, OA), rw);
        graph.associate(new NodeContext(bobUAID, UA), new NodeContext(bobRID, OA), r);
        graph.associate(new NodeContext(aliceUAID, UA), new NodeContext(aliceRWID, OA), rw);
        graph.associate(new NodeContext(aliceUAID, UA), new NodeContext(aliceRID, OA), r);

        // test configuration
        // create a decider
        // not using prohibitions in this example, so null is passed
        Decider decider = new PReviewDecider(graph, null);

        // user: bob
        // target: 'bob ssn'
        // expected: [r, w]
        // actual: [r, w]
        HashSet<String> permissions = decider.listPermissions(bobID, 0, bobSSNID);
        assertEquals(permissions, new HashSet<>(Arrays.asList("r", "w")));


        // user: bob
        // target: 'bob ssn'
        // expected: [r]
        // actual: [r]
        permissions = decider.listPermissions(bobID, 0, bobSalaryID);
        assertEquals(permissions, new HashSet<>(Arrays.asList("r")));

        // user: bob
        // target: 'alice ssn'
        // expected: []
        // actual: []
        permissions = decider.listPermissions(bobID, 0, aliceSSNID);
        assertTrue(permissions.isEmpty());

        // user: bob
        // target: 'alice salary'
        // expected: []
        // actual: []
        permissions = decider.listPermissions(bobID, 0, aliceSalaryID);
        assertTrue(permissions.isEmpty());

        // user: bob
        // target: 'bob ssn'
        // expected: [r, w]
        // actual: [r, w]
        permissions = decider.listPermissions(aliceID, 0, aliceSSNID);
        assertEquals(permissions, new HashSet<>(Arrays.asList("r", "w")));


        // user: charlie
        // target: 'alice salary'
        // expected: [r, w]
        // actual: [r, w]
        permissions = decider.listPermissions(charlieID, 0, aliceSalaryID);
        assertEquals(permissions, new HashSet<>(Arrays.asList("r", "w")));
    }
}
