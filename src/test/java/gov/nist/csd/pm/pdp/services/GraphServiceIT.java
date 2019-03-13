package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.common.exceptions.PMGraphException;
import gov.nist.csd.pm.common.util.NodeUtils;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.graph.model.nodes.NodeType;
import gov.nist.csd.pm.pap.PAP;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;

import static gov.nist.csd.pm.common.constants.Operations.*;
import static gov.nist.csd.pm.graph.model.nodes.NodeType.*;
import static gov.nist.csd.pm.utils.TestUtils.getDatabaseContext;
import static org.junit.jupiter.api.Assertions.*;

class GraphServiceIT {

    private static Node         superUser;
    private static Node         testUser;
    private static Node         testPC;
    private static Node         testOA;
    private static Node         testUA;
    private static Node         testO;
    private static String       testID;

    @BeforeEach
    void setup() throws PMException, IOException {
        PAP.getPAP(getDatabaseContext());
        superUser = PAP.getPAP().getSuperU();
        testID = UUID.randomUUID().toString();

        GraphService service = new GraphService(superUser.getID(), 0);

        // create a node for each type
        testPC = service.createNode(0, "pc1", PC, NodeUtils.toProperties("namespace", testID));
        testOA = service.createNode(testPC.getID(), "oa1", NodeType.OA, NodeUtils.toProperties("namespace", testID));
        testO = PAP.getPAP().getGraphPAP().createNode(new Random().nextLong(), "testO", O, NodeUtils.toProperties("namespace", testID));
        testUA = service.createNode(testPC.getID(), "ua1", UA, NodeUtils.toProperties("namespace", testID));
        testUser = PAP.getPAP().getGraphPAP().createNode(new Random().nextLong(), "u1", U, NodeUtils.toProperties("namespace", testID));

        // create assignments
        PAP.getPAP().getGraphPAP().assign(testO.getID(), testOA.getID());
        PAP.getPAP().getGraphPAP().assign(testUser.getID(), testUA.getID());
        PAP.getPAP().getGraphPAP().assign(superUser.getID(), testUA.getID());

        // create an association
        PAP.getPAP().getGraphPAP().associate(testUA.getID(), testOA.getID(), new HashSet<>(Arrays.asList(DISASSOCIATE, ASSOCIATE, ASSIGN, ASSIGN_TO, UPDATE_NODE, DELETE_NODE, DEASSIGN_FROM, DEASSIGN)));
    }

    @AfterEach
    void teardown() throws PMException {
        Set<Node> nodes = PAP.getPAP().getGraphPAP().search(null, null, NodeUtils.toProperties("namespace", testID));
        for(Node node : nodes) {
            PAP.getPAP().getGraphPAP().deleteNode(node.getID());
        }
    }

    @Test
    void TestCreateNode() throws PMException {
        // check exceptions are thrown
        final GraphService service1 = new GraphService(superUser.getID(), 0);
        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> service1.createNode(0, null, null, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> service1.createNode(0, null, OA, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> service1.createNode(0, "test_node", null, null)),
                () -> assertThrows(PMException.class, () -> service1.createNode(0, "oa1", NodeType.OA, NodeUtils.toProperties("namespace", testID))));

        // create a policy class
        // check that an unauthorized user cannot create a policy class
        final GraphService service2 = new GraphService(testUser.getID(), 0);
        assertThrows(PMException.class, () -> service2.createNode(0, "test_pc", PC, NodeUtils.toProperties("key1", "value1")));

        // authorized user - check all pc elements were created
        final GraphService service3 = new GraphService(superUser.getID(), 0);
        Node testPC = service3.createNode(0, "test_pc", PC, NodeUtils.toProperties("namespace", testID));
        assertTrue(testPC.getID() != 0, "the returned ID was 0");

        Node node = service3.getNode(testPC.getID());
        assertEquals(node.getID(), testPC.getID(), "node IDs do not match");
        assertEquals(node.getName(), "test_pc", "node name does not match");
        assertEquals(node.getType(), PC, "node types do not match");
        assertEquals(node.getProperties().get("namespace"), testID, "properties do not match");

        Set<Node> nodes = service3.search("test_pc rep", OA.toString(), null);
        assertFalse(nodes.isEmpty(), "the rep node could not be found");

        // create an OA
        // user can't assign to parent
        final GraphService service4 = new GraphService(testUser.getID(), 0);
        assertThrows(PMException.class, () -> service4.createNode(testPC.getID(), "newNode", OA, NodeUtils.toProperties("namespace", testID)));

        // user can assign to parent
        final GraphService service5 = new GraphService(superUser.getID(), 0);
        Node newNode = service5.createNode(testOA.getID(), "newNode", OA, NodeUtils.toProperties("namespace", testID));
        assertTrue(newNode.getID() != 0, "the returned ID for the OA newNode was 0");

        node = service5.getNode(newNode.getID());
        assertEquals(node.getID(), newNode.getID(), "node IDs do not match");
        assertEquals(node.getName(), "newNode", "node name oes not match");
        assertEquals(node.getType(), OA, "node types do not match");
        assertEquals(node.getProperties().get("namespace"), testID, "namespace does not match");
    }

    @Test
    void TestUpdateNode() throws PMException {
        // check exceptions are thrown
        final GraphService service1 = new GraphService(superUser.getID(), 0);

        service1.updateNode(testOA.getID(), null, NodeUtils.toProperties("test", "updated namespace"));

        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> service1.updateNode(0, "update_node_no_id", null)),
                () -> assertThrows(PMException.class, () -> service1.updateNode(new Random().nextLong(), "updated_name", NodeUtils.toProperties("updatedKey", "updatedValue"))));
        // user can update node
        service1.updateNode(testOA.getID(), "updated_name", NodeUtils.toProperties("updatedKey", "updatedValue", "namespace", testID));

        Node node = service1.getNode(testOA.getID());
        assertEquals(node.getName(), "updated_name", "node name does not match");
        assertEquals(node.getProperties(), NodeUtils.toProperties("updatedKey", "updatedValue", "namespace", testID), "properties do not match");

        // user cannot update node
        GraphService service2 = new GraphService(testUser.getID(), 0);
        assertThrows(PMException.class, () -> service2.updateNode(PAP.getPAP().getSuperO().getID(), "updated super name", null));
    }

    @Test
    void TestDeleteNode() throws PMException {
        // test user tries to delete super o
        GraphService service = new GraphService(testUser.getID(), 0);
        assertThrows(PMException.class, () -> service.deleteNode(PAP.getPAP().getSuperO().getID()));

        GraphService service1 = new GraphService(superUser.getID(), 0);
        // super user deletes test object
        service1.deleteNode(testO.getID());
        assertFalse(PAP.getPAP().getGraphPAP().exists(testO.getID()));

        // super user deletes test pc
        service1.deleteNode(testPC.getID());
        assertFalse(PAP.getPAP().getGraphPAP().exists(testPC.getID()));
    }

    @Test
    void TestExists() throws PMException {
        GraphService service = new GraphService(superUser.getID(), 0);
        assertTrue(service.exists(testPC.getID()));
        assertFalse(service.exists(new Random().nextLong()));
    }

   /* @Test
    void TestGetNodes() throws PMException {
        GraphService service = new GraphService(superUser.getID(), 0);
        HashSet<Node> nodes = service.getNodes();
        assertTrue(nodes.size() >= 7, "the super user should have access to at least 7 nodes");
        GraphService service1 = new GraphService(testUser.getID(), 0);
        HashSet<Node> nodes1 = service1.getNodes();
        assertEquals(2, nodes1.size(), "the test user should have access to 2 nodes");
    }*/

    @Test
    void TestGetPolicies() throws PMException {
        GraphService service = new GraphService(superUser.getID(), 0);
        Set<Long> policies = service.getPolicies();
        assertAll(() -> assertTrue(policies.contains(testPC.getID())),
                () -> assertTrue(policies.contains(PAP.getPAP().getSuperPC().getID())));
    }

    @Test
    void TestGetChildren() throws PMException {
        GraphService service = new GraphService(superUser.getID(), 0);
        assertThrows(PMException.class, () -> service.getChildren(new Random().nextLong()));

        Set<Node> children = service.getChildren(testOA.getID());
        assertEquals(1, children.size());
        assertEquals(children.iterator().next().getID(), testO.getID());
    }

    @Test
    void TestGetParents() throws PMException {
        GraphService service = new GraphService(superUser.getID(), 0);
        assertThrows(PMException.class, () -> service.getParents(new Random().nextLong()));

        Set<Node> parents = service.getParents(testO.getID());
        assertEquals(1, parents.size());
        assertEquals(parents.iterator().next().getID(), testOA.getID());
    }

    @Test
    void TestAssign() throws PMException {
        GraphService service = new GraphService(superUser.getID(), 0);
        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> service.assign(0, 0)),
                () -> assertThrows(IllegalArgumentException.class, () -> service.assign(123, 0)),
                () -> assertThrows(PMGraphException.class, () -> service.assign(123, 123)),
                () -> assertThrows(PMGraphException.class, () -> service.assign(testO.getID(), 123))
        );

        // super assigns test o to super oa
        service.assign(testO.getID(), PAP.getPAP().getSuperOA().getID());

        // super assigns super ua2 to test PC
        service.assign(PAP.getPAP().getSuperUA2().getID(), testPC.getID());

        // test user assigns test o to super oa
        GraphService service1 = new GraphService(testUser.getID(), 0);
        assertThrows(PMException.class,
                () -> service1.assign(testO.getID(), PAP.getPAP().getSuperOA().getID()));
    }

    @Test
    void TestDeassign() throws PMException {
        GraphService service = new GraphService(superUser.getID(), 0);
        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> service.deassign(0, 0)),
                () -> assertThrows(IllegalArgumentException.class, () -> service.deassign(123, 0)),
                () -> assertThrows(PMGraphException.class, () -> service.deassign(123, 123)),
                () -> assertThrows(PMGraphException.class, () -> service.deassign(testO.getID(), 123))
        );

        // super deassign test o from oa
        service.deassign(testO.getID(), testOA.getID());
        assertTrue(PAP.getPAP().getGraphPAP().getChildren(testOA.getID()).isEmpty());

        //test u deassign super o from super oa
        GraphService service1 = new GraphService(testUser.getID(), 0);
        assertThrows(PMException.class, () -> service1.deassign(PAP.getPAP().getSuperO().getID(), PAP.getPAP().getSuperOA().getID()));
    }

    @Test
    void TestAssociate() throws PMException {
        GraphService service = new GraphService(superUser.getID(), 0);
        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> service.associate(0, 0, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> service.associate(123, 0, null)),
                () -> assertThrows(PMGraphException.class, () -> service.associate(123, 123, null)),
                () -> assertThrows(PMGraphException.class, () -> service.associate(testO.getID(), 123, null))
        );

        service.associate(PAP.getPAP().getSuperUA2().getID(), testOA.getID(), new HashSet<>(Arrays.asList("test")));
        assertTrue(PAP.getPAP().getGraphPAP().getSourceAssociations(PAP.getPAP().getSuperUA2().getID()).containsKey(testOA.getID()));
        assertTrue(PAP.getPAP().getGraphPAP().getSourceAssociations(PAP.getPAP().getSuperUA2().getID()).get(testOA.getID()).contains("test"));
        service.associate(PAP.getPAP().getSuperUA2().getID(), testOA.getID(), new HashSet<>(Arrays.asList("test123")));
        assertTrue(PAP.getPAP().getGraphPAP().getSourceAssociations(PAP.getPAP().getSuperUA2().getID()).get(testOA.getID()).contains("test123"));

        GraphService service1 = new GraphService(testUser.getID(), 0);
        assertThrows(PMException.class,
                () -> service1.associate(PAP.getPAP().getSuperUA2().getID(), testOA.getID(), new HashSet<>(Arrays.asList("test"))));
    }

    @Test
    void TestDissociate() throws PMException {
        GraphService service = new GraphService(superUser.getID(), 0);
        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> service.dissociate(0, 0)),
                () -> assertThrows(IllegalArgumentException.class, () -> service.dissociate(123, 0)),
                () -> assertThrows(PMGraphException.class, () -> service.dissociate(123, 123)),
                () -> assertThrows(PMGraphException.class, () -> service.dissociate(testO.getID(), 123))
        );

        // super dissociate super ua2 and super oa
        service.dissociate(PAP.getPAP().getSuperUA2().getID(), PAP.getPAP().getSuperOA().getID());
        assertFalse(PAP.getPAP().getGraphPAP()
                        .getSourceAssociations(PAP.getPAP().getSuperUA2().getID())
                        .containsKey(PAP.getPAP().getSuperOA().getID()));

        // test user dissociates test ua and test oa
        GraphService service1 = new GraphService(testUser.getID(), 0);
        assertThrows(PMException.class, () -> service1.dissociate(testUA.getID(), testOA.getID()));
    }

    @Test
    void TestGetSourceAssociations() throws PMException {
        GraphService service = new GraphService(superUser.getID(), 0);
        assertThrows(PMException.class, () -> service.getSourceAssociations(new Random().nextLong()));

        Map<Long, Set<String>> assocs = service.getSourceAssociations(PAP.getPAP().getSuperUA2().getID());
        assertTrue(assocs.containsKey(PAP.getPAP().getSuperOA().getID()));
        assertTrue(assocs.get(PAP.getPAP().getSuperOA().getID()).contains("*"));

        GraphService service1 = new GraphService(testUser.getID(), 0);
        assertThrows(PMException.class, () -> service1.getSourceAssociations(PAP.getPAP().getSuperUA2().getID()));

    }

    @Test
    void TestGetTargetAssociations() throws PMException {
        GraphService service = new GraphService(superUser.getID(), 0);
        assertThrows(PMException.class, () -> service.getTargetAssociations(new Random().nextLong()));

        Map<Long, Set<String>> assocs = service.getTargetAssociations(PAP.getPAP().getSuperOA().getID());
        assertTrue(assocs.containsKey(PAP.getPAP().getSuperUA2().getID()));
        assertTrue(assocs.get(PAP.getPAP().getSuperUA2().getID()).contains("*"));

        GraphService service1 = new GraphService(testUser.getID(), 0);
        assertThrows(PMException.class, () -> service1.getTargetAssociations(PAP.getPAP().getSuperOA().getID()));
    }
}