package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.common.util.NodeUtils;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.graph.model.nodes.NodeType;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;

import static gov.nist.csd.pm.common.constants.Operations.*;
import static gov.nist.csd.pm.graph.model.nodes.NodeType.*;
import static gov.nist.csd.pm.pap.PAP.getPAP;
import static gov.nist.csd.pm.utils.TestUtils.getDatabaseContext;
import static org.junit.jupiter.api.Assertions.*;

class GraphServiceIT {

    private static long         superUserID;
    private static long         testUserID;
    private static long         testPCID;
    private static long         testOAID;
    private static long         testUAID;
    private static long         testOID;
    private static String       testID;

    @BeforeEach
    void setup() throws PMException, IOException {
        getPAP(getDatabaseContext());
        superUserID = getPAP().getSuperU().getID();
        testID = UUID.randomUUID().toString();

        GraphService service = new GraphService(superUserID, 0);

        // create a node for each type
        long pc1 = service.createNode(0, new Node("pc1", PC, NodeUtils.toProperties("namespace", testID)));
        testPCID = pc1;
        long oa1 = service.createNode(pc1, new Node("oa1", NodeType.OA, NodeUtils.toProperties("namespace", testID)));
        testOAID = oa1;
        long o1 = getPAP().getGraphPAP().createNode(new Node("o1", O, NodeUtils.toProperties("namespace", testID)));
        testOID = o1;
        long ua1 = service.createNode(pc1, new Node("ua1", UA, NodeUtils.toProperties("namespace", testID)));
        testUAID = ua1;
        long u1 = getPAP().getGraphPAP().createNode(new Node("u1", U, NodeUtils.toProperties("namespace", testID)));
        testUserID = u1;

        // create assignments
        getPAP().getGraphPAP().assign(new Node(o1, O), new Node(oa1, NodeType.OA));
        getPAP().getGraphPAP().assign(new Node(u1, U), new Node(ua1, UA));
        getPAP().getGraphPAP().assign(new Node(superUserID, U), new Node(ua1, UA));

        // create an association
        getPAP().getGraphPAP().associate(new Node(ua1, UA), new Node(oa1, NodeType.OA), new HashSet<>(Arrays.asList(DISASSOCIATE, ASSOCIATE, ASSIGN, ASSIGN_TO, UPDATE_NODE, DELETE_NODE, DEASSIGN_FROM, DEASSIGN)));
    }

    @AfterEach
    void teardown() throws PMException {
        Set<Node> nodes = getPAP().getGraphPAP().search(null, null, NodeUtils.toProperties("namespace", testID));
        for(Node node : nodes) {
            getPAP().getGraphPAP().deleteNode(node.getID());
        }
    }

    @Test
    void TestCreateNode() throws PMException {
        // check exceptions are thrown
        final GraphService service1 = new GraphService(superUserID, 0);
        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> service1.createNode(0, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> service1.createNode(0, new Node(null, OA, null))),
                () -> assertThrows(IllegalArgumentException.class, () -> service1.createNode(0, new Node("test_node", null, null))),
                () -> assertThrows(PMException.class, () -> service1.createNode(0, new Node("oa1", NodeType.OA, NodeUtils.toProperties("namespace", testID)))));

        // create a policy class
        // check that an unauthorized user cannot create a policy class
        final GraphService service2 = new GraphService(testUserID, 0);
        assertThrows(PMException.class, () -> service2.createNode(0, new Node("test_pc", PC, NodeUtils.toProperties("key1", "value1"))));

        // authorized user - check all pc elements were created
        final GraphService service3 = new GraphService(superUserID, 0);
        long id = service3.createNode(0, new Node("test_pc", PC, NodeUtils.toProperties("namespace", testID)));
        assertTrue(id != 0, "the returned ID was 0");

        Node node = service3.getNode(id);
        assertEquals(node.getID(), id, "node IDs do not match");
        assertEquals(node.getName(), "test_pc", "node name does not match");
        assertEquals(node.getType(), PC, "node types do not match");
        assertEquals(node.getProperties().get("namespace"), testID, "properties do not match");

        Set<Node> nodes = service3.search("test_pc rep", OA.toString(), null);
        assertFalse(nodes.isEmpty(), "the rep node could not be found");

        // create an OA
        // user can't assign to parent
        final GraphService service4 = new GraphService(testUserID, 0);
        assertThrows(PMException.class, () -> service4.createNode(testPCID, new Node().name("newNode").type(OA).properties(NodeUtils.toProperties("namespace", testID))));

        // user can assign to parent
        final GraphService service5 = new GraphService(superUserID, 0);
        id = service5.createNode(testOAID, new Node().name("newNode").type(OA).properties(NodeUtils.toProperties("namespace", testID)));
        assertTrue(id != 0, "the returned ID for the OA newNode was 0");

        node = service5.getNode(id);
        assertEquals(node.getID(), id, "node IDs do not match");
        assertEquals(node.getName(), "newNode", "node name oes not match");
        assertEquals(node.getType(), OA, "node types do not match");
        assertEquals(node.getProperties().get("namespace"), testID, "namespace does not match");
    }

    @Test
    void TestUpdateNode() throws PMException {
        // check exceptions are thrown
        final GraphService service1 = new GraphService(superUserID, 0);

        service1.updateNode(new Node().id(testOAID).properties(NodeUtils.toProperties("test", "updated namespace")));

        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> service1.updateNode(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> service1.updateNode(new Node("update_node_no_id", null, null))),
                () -> assertThrows(PMException.class, () -> service1.updateNode(
                        new Node().id(new Random().nextLong()).name("updated_name").properties(NodeUtils.toProperties("updatedKey", "updatedValue")))));
        // user can update node
        service1.updateNode(new Node().id(testOAID).name("updated_name").properties(NodeUtils.toProperties("updatedKey", "updatedValue", "namespace", testID)));

        Node node = service1.getNode(testOAID);
        assertEquals(node.getName(), "updated_name", "node name does not match");
        assertEquals(node.getProperties(), NodeUtils.toProperties("updatedKey", "updatedValue", "namespace", testID), "properties do not match");

        // user cannot update node
        GraphService service2 = new GraphService(testUserID, 0);
        assertThrows(PMException.class, () -> service2.updateNode(new Node().id(getPAP().getSuperO().getID()).name("updated super name")));
    }

    @Test
    void TestDeleteNode() throws PMException {
        // test user tries to delete super o
        GraphService service = new GraphService(testUserID, 0);
        assertThrows(PMException.class, () -> service.deleteNode(getPAP().getSuperO().getID()));

        GraphService service1 = new GraphService(superUserID, 0);
        // super user deletes test object
        service1.deleteNode(testOID);
        assertFalse(getPAP().getGraphPAP().exists(testOID));

        // super user deletes test pc
        service1.deleteNode(testPCID);
        assertFalse(getPAP().getGraphPAP().exists(testPCID));
    }

    @Test
    void TestExists() throws PMException {
        GraphService service = new GraphService(superUserID, 0);
        assertTrue(service.exists(testPCID));
        assertFalse(service.exists(new Random().nextLong()));
    }

   /* @Test
    void TestGetNodes() throws PMException {
        GraphService service = new GraphService(superUserID, 0);
        HashSet<Node> nodes = service.getNodes();
        assertTrue(nodes.size() >= 7, "the super user should have access to at least 7 nodes");
        GraphService service1 = new GraphService(testUserID, 0);
        HashSet<Node> nodes1 = service1.getNodes();
        assertEquals(2, nodes1.size(), "the test user should have access to 2 nodes");
    }*/

    @Test
    void TestGetPolicies() throws PMException {
        GraphService service = new GraphService(superUserID, 0);
        Set<Long> policies = service.getPolicies();
        assertAll(() -> assertTrue(policies.contains(testPCID)),
                () -> assertTrue(policies.contains(getPAP().getSuperPC().getID())));
    }

    @Test
    void TestGetChildren() throws PMException {
        GraphService service = new GraphService(superUserID, 0);
        assertThrows(PMException.class, () -> service.getChildren(new Random().nextLong()));

        Set<Node> children = service.getChildren(testOAID);
        assertEquals(1, children.size());
        assertEquals(children.iterator().next().getID(), testOID);
    }

    @Test
    void TestGetParents() throws PMException {
        GraphService service = new GraphService(superUserID, 0);
        assertThrows(PMException.class, () -> service.getParents(new Random().nextLong()));

        Set<Node> parents = service.getParents(testOID);
        assertEquals(1, parents.size());
        assertEquals(parents.iterator().next().getID(), testOAID);
    }

    @Test
    void TestAssign() throws PMException {
        GraphService service = new GraphService(superUserID, 0);
        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> service.assign(null, new Node())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.assign(new Node(), null)),
                () -> assertThrows(IllegalArgumentException.class, () -> service.assign(new Node(), new Node())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.assign(new Node().id(123), new Node())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.assign(new Node().id(123).type(OA), new Node())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.assign(new Node().id(123).type(OA), new Node().id(123))),
                () -> assertThrows(PMException.class, () -> service.assign(new Node().id(new Random().nextLong()).type(OA), new Node().id(new Random().nextLong()).type(OA))),
                () -> assertThrows(PMException.class, () -> service.assign(new Node().id(new Random().nextLong()).type(OA), new Node().id(new Random().nextLong()).type(OA))),

                () -> assertThrows(PMException.class, () -> service.assign(new Node().id(new Random().nextLong()).type(PC), new Node().id(new Random().nextLong()).type(OA))),
                () -> assertThrows(PMException.class, () -> service.assign(new Node().id(new Random().nextLong()).type(OA), new Node().id(new Random().nextLong()).type(UA))),
                () -> assertThrows(PMException.class, () -> service.assign(new Node().id(new Random().nextLong()).type(O), new Node().id(new Random().nextLong()).type(U))),
                () -> assertThrows(PMException.class, () -> service.assign(new Node().id(new Random().nextLong()).type(UA), new Node().id(new Random().nextLong()).type(OA))),
                () -> assertThrows(PMException.class, () -> service.assign(new Node().id(new Random().nextLong()).type(U), new Node().id(new Random().nextLong()).type(OA)))
        );

        // super assigns test o to super oa
        service.assign(new Node().id(testOID).type(O), getPAP().getSuperOA());

        // super assigns super ua2 to test PC
        service.assign(getPAP().getSuperUA2(), new Node().id(testPCID).type(PC));

        // test user assigns test o to super oa
        GraphService service1 = new GraphService(testUserID, 0);
        assertThrows(PMException.class,
                () -> service1.assign(new Node().id(testOID).type(O), new Node().id(getPAP().getSuperOA().getID()).type(OA)));
    }

    @Test
    void TestDeassign() throws PMException {
        GraphService service = new GraphService(superUserID, 0);
        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> service.deassign(null, new Node())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.assign(new Node(), null)),
                () -> assertThrows(IllegalArgumentException.class, () -> service.assign(new Node(), new Node())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.assign(new Node().id(123), new Node())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.assign(new Node().id(123).type(OA), new Node())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.assign(new Node().id(123).type(OA), new Node().id(123))),
                () -> assertThrows(PMException.class, () -> service.assign(new Node().id(new Random().nextLong()).type(OA), new Node().id(new Random().nextLong()).type(OA))),
                () -> assertThrows(PMException.class, () -> service.assign(new Node().id(new Random().nextLong()).type(OA), new Node().id(new Random().nextLong()).type(OA)))
        );

        // super deassign test o from oa
        service.deassign(new Node().id(testOID).type(O), new Node().id(testOAID).type(OA));
        assertTrue(getPAP().getGraphPAP().getChildren(testOAID).isEmpty());

        //test u deassign super o from super oa
        GraphService service1 = new GraphService(testUserID, 0);
        assertThrows(PMException.class, () -> service1.deassign(getPAP().getSuperO(), getPAP().getSuperOA()));
    }

    @Test
    void TestAssociate() throws PMException {
        GraphService service = new GraphService(superUserID, 0);
        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> service.associate(null, new Node(), new HashSet<>())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.associate(new Node(), null, new HashSet<>())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.associate(new Node(), new Node(), new HashSet<>())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.associate(new Node().id(123), new Node(), new HashSet<>())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.associate(new Node().id(123).type(OA), new Node(), new HashSet<>())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.associate(new Node().id(123).type(OA), new Node().id(123), new HashSet<>())),
                () -> assertThrows(PMException.class, () -> service.associate(new Node().id(new Random().nextLong()).type(OA), new Node().id(new Random().nextLong()).type(OA), new HashSet<>())),
                () -> assertThrows(PMException.class, () -> service.associate(new Node().id(new Random().nextLong()).type(OA), new Node().id(new Random().nextLong()).type(OA), new HashSet<>())),

                () -> assertThrows(PMException.class, () -> service.associate(new Node().id(new Random().nextLong()).type(PC), new Node().id(new Random().nextLong()).type(OA), new HashSet<>())),
                () -> assertThrows(PMException.class, () -> service.associate(new Node().id(new Random().nextLong()).type(OA), new Node().id(new Random().nextLong()).type(UA), new HashSet<>())),
                () -> assertThrows(PMException.class, () -> service.associate(new Node().id(new Random().nextLong()).type(O), new Node().id(new Random().nextLong()).type(U), new HashSet<>())),
                () -> assertThrows(PMException.class, () -> service.associate(new Node().id(new Random().nextLong()).type(UA), new Node().id(new Random().nextLong()).type(O), new HashSet<>())),
                () -> assertThrows(PMException.class, () -> service.associate(new Node().id(new Random().nextLong()).type(U), new Node().id(new Random().nextLong()).type(OA), new HashSet<>()))
        );

        service.associate(getPAP().getSuperUA2(), new Node().id(testOAID).type(OA), new HashSet<>(Arrays.asList("test")));
        assertTrue(getPAP().getGraphPAP().getSourceAssociations(getPAP().getSuperUA2().getID()).containsKey(testOAID));
        assertTrue(getPAP().getGraphPAP().getSourceAssociations(getPAP().getSuperUA2().getID()).get(testOAID).contains("test"));
        service.associate(getPAP().getSuperUA2(), new Node().id(testOAID).type(OA), new HashSet<>(Arrays.asList("test123")));
        assertTrue(getPAP().getGraphPAP().getSourceAssociations(getPAP().getSuperUA2().getID()).get(testOAID).contains("test123"));

        GraphService service1 = new GraphService(testUserID, 0);
        assertThrows(PMException.class,
                () -> service1.associate(getPAP().getSuperUA2(), new Node().id(testOAID).type(OA), new HashSet<>(Arrays.asList("test"))));
    }

    @Test
    void TestDissociate() throws PMException {
        GraphService service = new GraphService(superUserID, 0);
        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> service.associate(null, new Node(), new HashSet<>())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.associate(new Node(), null, new HashSet<>())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.associate(new Node(), new Node(), new HashSet<>())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.associate(new Node().id(123), new Node(), new HashSet<>())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.associate(new Node().id(123).type(OA), new Node(), new HashSet<>())),
                () -> assertThrows(IllegalArgumentException.class, () -> service.associate(new Node().id(123).type(OA), new Node().id(123), new HashSet<>())),
                () -> assertThrows(PMException.class, () -> service.associate(new Node().id(new Random().nextLong()).type(OA), new Node().id(new Random().nextLong()).type(OA), new HashSet<>())),
                () -> assertThrows(PMException.class, () -> service.associate(new Node().id(new Random().nextLong()).type(OA), new Node().id(new Random().nextLong()).type(OA), new HashSet<>()))
        );

        // super dissociate super ua2 and super oa
        service.dissociate(getPAP().getSuperUA2(), getPAP().getSuperOA());
        assertFalse(getPAP().getGraphPAP().getSourceAssociations(getPAP().getSuperUA2().getID()).containsKey(getPAP().getSuperOA().getID()));

        // test user dissociates test ua and test oa
        GraphService service1 = new GraphService(testUserID, 0);
        assertThrows(PMException.class,
                () -> service1.dissociate(new Node().id(testUAID).type(UA), new Node().id(testOAID).type(OA)));
    }

    @Test
    void TestGetSourceAssociations() throws PMException {
        GraphService service = new GraphService(superUserID, 0);
        assertThrows(PMException.class, () -> service.getSourceAssociations(new Random().nextLong()));

        Map<Long, Set<String>> assocs = service.getSourceAssociations(getPAP().getSuperUA2().getID());
        assertTrue(assocs.containsKey(getPAP().getSuperOA().getID()));
        assertTrue(assocs.get(getPAP().getSuperOA().getID()).contains("*"));

        GraphService service1 = new GraphService(testUserID, 0);
        assertThrows(PMException.class, () -> service1.getSourceAssociations(getPAP().getSuperUA2().getID()));

    }

    @Test
    void TestGetTargetAssociations() throws PMException {
        GraphService service = new GraphService(superUserID, 0);
        assertThrows(PMException.class, () -> service.getTargetAssociations(new Random().nextLong()));

        Map<Long, Set<String>> assocs = service.getTargetAssociations(getPAP().getSuperOA().getID());
        assertTrue(assocs.containsKey(getPAP().getSuperUA2().getID()));
        assertTrue(assocs.get(getPAP().getSuperUA2().getID()).contains("*"));

        GraphService service1 = new GraphService(testUserID, 0);
        assertThrows(PMException.class, () -> service1.getTargetAssociations(getPAP().getSuperOA().getID()));
    }
}