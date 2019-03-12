package gov.nist.csd.pm.pip.graph;

import gov.nist.csd.pm.common.util.NodeUtils;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.search.Neo4jSearch;
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
    private Neo4jSearch search;
    private String testID;

    @BeforeEach
    public void setUp() throws PMException, IOException {
        graph = new Neo4jGraph(TestUtils.getDatabaseContext());
        search = new Neo4jSearch(TestUtils.getDatabaseContext());
        testID = UUID.randomUUID().toString();
    }

    @AfterEach
    public void tearDown() throws PMException, IOException {
        Set<Node> nodes = new Neo4jSearch(TestUtils.getDatabaseContext()).search(null, null, NodeUtils.toProperties("namespace", testID));
        for(Node node : nodes) {
            graph.deleteNode(node.getID());
        }
    }

    @Test
    public void testCreateNode() throws PMException {
        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> graph.createNode(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.createNode(new Node(null, OA, null))),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.createNode(new Node("", OA, null))),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.createNode(new Node("name", null, null)))
        );

        // add pc
        long pc = graph.createNode(new Node("pc", PC, NodeUtils.toProperties("namespace", testID)));
        assertTrue(graph.getPolicies().contains(pc));

        // add non pc
        long nodeID = graph.createNode(new Node("oa", OA, NodeUtils.toProperties("namespace", testID)));

        // check node is added
        Node node = search.getNode(nodeID);
        assertEquals("oa", node.getName());
        assertEquals(OA, node.getType());
    }

    @Test
    public void testUpdateNode() throws PMException {
        Node node = new Node("node", OA, NodeUtils.toProperties("namespace", testID));
        long nodeID = graph.createNode(node);
        node.id(nodeID);

        // node not found
        assertThrows(PMException.class, () -> graph.updateNode(new Node(new Random().nextLong(), "newNodeName", null, null)));

        // update name
        graph.updateNode(node.name("updated name"));
        assertEquals(search.getNode(nodeID).getName(), "updated name");

        // update properties
        graph.updateNode(node.property("newKey", "newValue"));
        assertEquals(search.getNode(nodeID).getProperties().get("newKey"), "newValue");
    }

    @Test
    public void testDeleteNode() throws PMException {
        long id = graph.createNode(new Node("node", PC, NodeUtils.toProperties("namespace", testID)));

        graph.deleteNode(id);

        // deleted from the graph
        assertFalse(graph.exists(id));

        // deleted from the node map

        // deleted from list of policies
        assertFalse(graph.getPolicies().contains(id));
    }

    @Test
    public void testExists() throws PMException {
        long id = graph.createNode(new Node("node", OA, NodeUtils.toProperties("namespace", testID)));
        assertTrue(graph.exists(id));
        assertFalse(graph.exists(new Random().nextLong()));
    }

    @Test
    public void testGetNodes() throws PMException {
        long node1 = graph.createNode(new Node("node1", OA, NodeUtils.toProperties("namespace", testID)));
        long node2 = graph.createNode(new Node("node2", OA, NodeUtils.toProperties("namespace", testID)));
        long node3 = graph.createNode(new Node("node3", OA, NodeUtils.toProperties("namespace", testID)));

        assertTrue(graph.getNodes().containsAll(Arrays.asList(new Node().id(node1), new Node().id(node2), new Node().id(node3))));
    }

    @Test
    public void testGetPolicies() throws PMException {
        long node1 = graph.createNode(new Node("node1", PC, NodeUtils.toProperties("namespace", testID)));
        long node2 = graph.createNode(new Node("node2", PC, NodeUtils.toProperties("namespace", testID)));
        long node3 = graph.createNode(new Node("node3", PC, NodeUtils.toProperties("namespace", testID)));

        assertTrue(graph.getPolicies().containsAll(Arrays.asList(node1, node2, node3)));
    }

    @Test
    public void testGetChildren() throws PMException {
        long parentID = graph.createNode(new Node("parent", OA, NodeUtils.toProperties("namespace", testID)));
        long child1ID = graph.createNode(new Node("child1", OA, NodeUtils.toProperties("namespace", testID)));
        long child2ID = graph.createNode(new Node("child2", OA, NodeUtils.toProperties("namespace", testID)));

        graph.assign(new Node(child1ID, OA), new Node(parentID, OA));
        graph.assign(new Node(child2ID, OA), new Node(parentID, OA));

        Set<Long> children = graph.getChildren(parentID);
        assertTrue(children.contains(child1ID));
        assertTrue(children.contains(child2ID));
    }

    @Test
    public void testGetParents() throws PMException {
        long parent1ID = graph.createNode(new Node("parent1", OA, NodeUtils.toProperties("namespace", testID)));
        long parent2ID = graph.createNode(new Node("parent2", OA, NodeUtils.toProperties("namespace", testID)));
        long child1ID = graph.createNode(new Node("child1", OA, NodeUtils.toProperties("namespace", testID)));

        graph.assign(new Node(child1ID, OA), new Node(parent1ID, OA));
        graph.assign(new Node(child1ID, OA), new Node(parent2ID, OA));

        Set<Long> children = graph.getParents(child1ID);
        assertTrue(children.contains(parent1ID));
        assertTrue(children.contains(parent2ID));
    }

    @Test
    public void testAssign() throws PMException {
        long parent1ID = graph.createNode(new Node("parent1", OA, NodeUtils.toProperties("namespace", testID)));
        long child1ID = graph.createNode(new Node("child1", OA, NodeUtils.toProperties("namespace", testID)));

        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> graph.assign(null, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.assign(new Node(), null)),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.assign(new Node().id(new Random().nextLong()), null)),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.assign(new Node().id(child1ID), new Node().id(new Random().nextLong())))
        );

        graph.assign(new Node(child1ID, OA), new Node(parent1ID, OA));

        assertTrue(graph.getChildren(parent1ID).contains(child1ID));
        assertTrue(graph.getParents(child1ID).contains(parent1ID));
    }

    @Test
    public void testDeassign() throws PMException {
        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> graph.assign(null, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.assign(new Node(), null))
        );

        long parent1ID = graph.createNode(new Node("parent1", OA, NodeUtils.toProperties("namespace", testID)));
        long child1ID = graph.createNode(new Node("child1", OA, NodeUtils.toProperties("namespace", testID)));

        graph.assign(new Node(child1ID, OA), new Node(parent1ID, OA));
        graph.deassign(new Node(child1ID, OA), new Node(parent1ID, OA));

        assertFalse(graph.getChildren(parent1ID).contains(child1ID));
        assertFalse(graph.getParents(child1ID).contains(parent1ID));
    }

    @Test
    public void testAssociate() throws PMException {
        long uaID = graph.createNode(new Node("ua", UA, NodeUtils.toProperties("namespace", testID)));
        long targetID = graph.createNode(new Node("target", OA, NodeUtils.toProperties("namespace", testID)));

        graph.associate(new Node(uaID, UA), new Node(targetID, OA), new HashSet<>(Arrays.asList("read", "write")));

        Map<Long, Set<String>> associations = graph.getSourceAssociations(uaID);
        assertTrue(associations.containsKey(targetID));
        assertTrue(associations.get(targetID).containsAll(Arrays.asList("read", "write")));

        associations = graph.getTargetAssociations(targetID);
        assertTrue(associations.containsKey(uaID));
        assertTrue(associations.get(uaID).containsAll(Arrays.asList("read", "write")));
    }

    @Test
    public void testDissociate() throws PMException {
        long uaID = graph.createNode(new Node(1, "ua", UA, NodeUtils.toProperties("namespace", testID)));
        long targetID = graph.createNode(new Node(3, "target", OA, NodeUtils.toProperties("namespace", testID)));

        graph.associate(new Node(uaID, UA), new Node(targetID, OA), new HashSet<>(Arrays.asList("read", "write")));
        graph.dissociate(new Node(uaID, UA), new Node(targetID, OA));

        Map<Long, Set<String>> associations = graph.getSourceAssociations(uaID);
        assertFalse(associations.containsKey(targetID));

        associations = graph.getTargetAssociations(targetID);
        assertFalse(associations.containsKey(targetID));
    }

    @Test
    public void testGetSourceAssociations() throws PMException {
        long uaID = graph.createNode(new Node(1, "ua", UA, NodeUtils.toProperties("namespace", testID)));
        long targetID = graph.createNode(new Node(3, "target", OA, NodeUtils.toProperties("namespace", testID)));

        graph.associate(new Node(uaID, UA), new Node(targetID, OA), new HashSet<>(Arrays.asList("read", "write")));
        graph.dissociate(new Node(uaID, UA), new Node(targetID, OA));

        Map<Long, Set<String>> associations = graph.getSourceAssociations(uaID);
        assertFalse(associations.containsKey(targetID));
    }

    @Test
    public void testGetTargetAssociations() throws PMException {
        long uaID = graph.createNode(new Node(1, "ua", UA, NodeUtils.toProperties("namespace", testID)));
        long targetID = graph.createNode(new Node(3, "target", OA, NodeUtils.toProperties("namespace", testID)));

        graph.associate(new Node(uaID, UA), new Node(targetID, OA), new HashSet<>(Arrays.asList("read", "write")));
        graph.dissociate(new Node(uaID, UA), new Node(targetID, OA));

        Map<Long, Set<String>> associations = graph.getTargetAssociations(targetID);
        assertFalse(associations.containsKey(uaID));
    }
}