package gov.nist.csd.pm.pip.graph;

import gov.nist.csd.pm.common.exceptions.PMGraphException;
import gov.nist.csd.pm.common.util.NodeUtils;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static gov.nist.csd.pm.graph.model.nodes.NodeType.OA;
import static gov.nist.csd.pm.graph.model.nodes.NodeType.PC;
import static gov.nist.csd.pm.graph.model.nodes.NodeType.UA;
import static org.junit.jupiter.api.Assertions.*;

public class Neo4jGraphIT {

    private Neo4jGraph graph;
    private String testID;
    private Random random = new Random();

    @BeforeEach
    public void setUp() throws PMException, IOException {
        graph = new Neo4jGraph(TestUtils.getDatabaseContext());
        testID = UUID.randomUUID().toString();
    }

    @AfterEach
    public void tearDown() throws PMException, IOException {
        Set<Node> nodes = graph.search(null, null, NodeUtils.toProperties("namespace", testID));
        for(Node node : nodes) {
            graph.deleteNode(node.getID());
        }
    }

    @Test
    public void testCreateNode() throws PMException {
        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> graph.createNode(0, "", OA, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.createNode(123, null, null, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.createNode(123, "name", null, null))
        );

        // add pc
        Node pc = graph.createNode(random.nextLong(), "pc", PC, NodeUtils.toProperties("namespace", testID));
        assertTrue(graph.getPolicies().contains(pc.getID()));

        // add non pc
        Node node = graph.createNode(random.nextLong(), "oa", OA, NodeUtils.toProperties("namespace", testID));

        // check node is added
        node = graph.getNode(node.getID());
        assertEquals("oa", node.getName());
        assertEquals(OA, node.getType());
    }

    @Test
    public void testUpdateNode() throws PMException {
        Node node = graph.createNode(random.nextLong(), "node", OA, Node.toProperties("namespace", testID));

        // node not found
        assertThrows(PMException.class, () -> graph.updateNode(random.nextLong(), "newNodeName", null));

        // update name
        graph.updateNode(node.getID(), "updated name", null);
        assertEquals(graph.getNode(node.getID()).getName(), "updated name");

        // update properties
        graph.updateNode(node.getID(), null, Node.toProperties("newKey", "newValue"));
        assertEquals(graph.getNode(node.getID()).getProperties().get("newKey"), "newValue");
    }

    @Test
    public void testDeleteNode() throws PMException {
        Node node = graph.createNode(random.nextLong(), "node", PC, Node.toProperties("namespace", testID));

        graph.deleteNode(node.getID());

        // deleted from the graph
        assertFalse(graph.exists(node.getID()));

        assertThrows(PMException.class, () -> graph.getNode(node.getID()));

        // deleted from list of policies
        assertFalse(graph.getPolicies().contains(node.getID()));
    }

    @Test
    public void testExists() throws PMException {
        Node node = graph.createNode(random.nextLong(), "node", OA, Node.toProperties("namespace", testID));
        assertTrue(graph.exists(node.getID()));
        assertFalse(graph.exists(random.nextLong()));
    }

    @Test
    public void testGetNodes() throws PMException {
        Node node1 = graph.createNode(new Random().nextLong(), "node1", OA, NodeUtils.toProperties("namespace", testID));
        Node node2 = graph.createNode(new Random().nextLong(), "node2", OA, NodeUtils.toProperties("namespace", testID));
        Node node3 = graph.createNode(new Random().nextLong(), "node3", OA, NodeUtils.toProperties("namespace", testID));

        assertTrue(graph.getNodes().containsAll(Arrays.asList(node1, node2, node3)));
    }

    @Test
    public void testGetPolicies() throws PMException {
        Node node1 = graph.createNode(new Random().nextLong(), "node1", PC, NodeUtils.toProperties("namespace", testID));
        Node node2 = graph.createNode(new Random().nextLong(), "node2", PC, NodeUtils.toProperties("namespace", testID));
        Node node3 = graph.createNode(new Random().nextLong(), "node3", PC, NodeUtils.toProperties("namespace", testID));

        assertTrue(graph.getPolicies().containsAll(Arrays.asList(node1.getID(), node2.getID(), node3.getID())));
    }

    @Test
    public void testGetChildren() throws PMException {
        Node parent = graph.createNode(random.nextLong(), "parent", OA, NodeUtils.toProperties("namespace", testID));
        Node child1 = graph.createNode(random.nextLong(), "child1", OA, NodeUtils.toProperties("namespace", testID));
        Node child2 = graph.createNode(random.nextLong(), "child2", OA, NodeUtils.toProperties("namespace", testID));

        graph.assign(child1.getID(), parent.getID());
        graph.assign(child2.getID(), parent.getID());

        Set<Long> children = graph.getChildren(parent.getID());
        assertTrue(children.contains(child1.getID()));
        assertTrue(children.contains(child2.getID()));
    }

    @Test
    public void testGetParents() throws PMException {
        Node parent1 = graph.createNode(random.nextLong(), "parent1", OA, NodeUtils.toProperties("namespace", testID));
        Node parent2 = graph.createNode(random.nextLong(), "parent2", OA, NodeUtils.toProperties("namespace", testID));
        Node child1 = graph.createNode(random.nextLong(), "child1", OA, NodeUtils.toProperties("namespace", testID));

        graph.assign(child1.getID(), parent1.getID());
        graph.assign(child1.getID(), parent2.getID());

        Set<Long> children = graph.getParents(child1.getID());
        assertTrue(children.contains(parent1.getID()));
        assertTrue(children.contains(parent2.getID()));
    }

    @Test
    public void testAssign() throws PMException {
        Node parent1 = graph.createNode(random.nextLong(), "parent1", OA, NodeUtils.toProperties("namespace", testID));
        Node child1 = graph.createNode(random.nextLong(), "child1", OA, NodeUtils.toProperties("namespace", testID));
        Node ua = graph.createNode(random.nextLong(), "ua", UA, NodeUtils.toProperties("namespace", testID));

        graph.assign(child1.getID(), parent1.getID());

        assertThrows(PMException.class, () -> graph.assign(ua.getID(), parent1.getID()));

        assertTrue(graph.getChildren(parent1.getID()).contains(child1.getID()));
        assertTrue(graph.getParents(child1.getID()).contains(parent1.getID()));
    }

    @Test
    public void testDeassign() throws PMException {
        assertAll(() -> assertThrows(PMGraphException.class, () -> graph.assign(0, 0)),
                () -> assertThrows(PMGraphException.class, () -> graph.assign(random.nextLong(), random.nextLong()))
        );

        Node parent1 = graph.createNode(random.nextLong(), "parent1", OA, NodeUtils.toProperties("namespace", testID));
        Node child1 = graph.createNode(random.nextLong(), "child1", OA, NodeUtils.toProperties("namespace", testID));

        graph.assign(child1.getID(), parent1.getID());
        graph.deassign(child1.getID(), parent1.getID());

        assertFalse(graph.getChildren(parent1.getID()).contains(child1.getID()));
        assertFalse(graph.getParents(child1.getID()).contains(parent1.getID()));
    }

    @Test
    public void testAssociate() throws PMException {
        Node ua = graph.createNode(random.nextLong(), "ua", UA, NodeUtils.toProperties("namespace", testID));
        Node target = graph.createNode(random.nextLong(), "target", OA, NodeUtils.toProperties("namespace", testID));

        graph.associate(ua.getID(), target.getID(), new HashSet<>(Arrays.asList("read", "write")));

        Map<Long, Set<String>> associations = graph.getSourceAssociations(ua.getID());
        assertTrue(associations.containsKey(target.getID()));
        assertTrue(associations.get(target.getID()).containsAll(Arrays.asList("read", "write")));

        associations = graph.getTargetAssociations(target.getID());
        assertTrue(associations.containsKey(ua.getID()));
        assertTrue(associations.get(ua.getID()).containsAll(Arrays.asList("read", "write")));
    }

    @Test
    public void testDissociate() throws PMException {
        Node ua = graph.createNode(random.nextLong(), "ua", UA, NodeUtils.toProperties("namespace", testID));
        Node target = graph.createNode(random.nextLong(), "target", OA, NodeUtils.toProperties("namespace", testID));

        graph.associate(ua.getID(), target.getID(), new HashSet<>(Arrays.asList("read", "write")));
        graph.dissociate(ua.getID(), target.getID());

        Map<Long, Set<String>> associations = graph.getSourceAssociations(ua.getID());
        assertFalse(associations.containsKey(target.getID()));

        associations = graph.getTargetAssociations(target.getID());
        assertFalse(associations.containsKey(target.getID()));
    }

    @Test
    public void testGetSourceAssociations() throws PMException {
        Node ua = graph.createNode(random.nextLong(), "ua", UA, NodeUtils.toProperties("namespace", testID));
        Node target = graph.createNode(random.nextLong(), "target", OA, NodeUtils.toProperties("namespace", testID));

        graph.associate(ua.getID(), target.getID(), new HashSet<>(Arrays.asList("read", "write")));
        graph.dissociate(ua.getID(), target.getID());

        Map<Long, Set<String>> associations = graph.getSourceAssociations(ua.getID());
        assertFalse(associations.containsKey(target.getID()));
    }

    @Test
    public void testGetTargetAssociations() throws PMException {
        Node ua = graph.createNode(random.nextLong(), "ua", UA, NodeUtils.toProperties("namespace", testID));
        Node target = graph.createNode(random.nextLong(), "target", OA, NodeUtils.toProperties("namespace", testID));

        graph.associate(ua.getID(), target.getID(), new HashSet<>(Arrays.asList("read", "write")));
        graph.dissociate(ua.getID(), target.getID());

        Map<Long, Set<String>> associations = graph.getTargetAssociations(target.getID());
        assertFalse(associations.containsKey(ua.getID()));
    }

    @Test
    void testSearch() throws PMException {
        graph.createNode(random.nextLong(), "oa1", OA, NodeUtils.toProperties("namespace", testID));
        graph.createNode(random.nextLong(), "oa2", OA, NodeUtils.toProperties("namespace", testID, "key1", "value1"));
        graph.createNode(random.nextLong(), "oa3", OA, NodeUtils.toProperties("namespace", testID, "key1", "value1", "key2", "value2"));

        // name and type no properties
        Set<Node> nodes = graph.search("oa1", OA.toString(), NodeUtils.toProperties("namespace", testID));
        assertEquals(1, nodes.size());

        // one property
        nodes = graph.search(null, null, NodeUtils.toProperties("key1", "value1"));
        assertEquals(2, nodes.size());

        // shared property
        nodes = graph.search(null, null, NodeUtils.toProperties("namespace", testID));
        assertEquals(3, nodes.size());
    }

    @Test
    void testGetNode() throws PMException {
        assertThrows(PMException.class, () -> graph.getNode(123));

        Node node = graph.createNode(random.nextLong(), "oa1", OA, NodeUtils.toProperties("namespace", testID));
        node = graph.getNode(node.getID());
        assertEquals("oa1", node.getName());
        assertEquals(OA, node.getType());
        assertEquals(NodeUtils.toProperties("namespace", testID), node.getProperties());
    }
}