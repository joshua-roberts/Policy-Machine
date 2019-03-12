package gov.nist.csd.pm.pip.graph;

import gov.nist.csd.pm.common.util.NodeUtils;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.Graph;
import gov.nist.csd.pm.graph.MemGraph;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.graph.model.nodes.NodeType;
import gov.nist.csd.pm.graph.model.relationships.Assignment;
import gov.nist.csd.pm.graph.model.relationships.Association;
import gov.nist.csd.pm.pip.loader.graph.Neo4jGraphLoader;
import gov.nist.csd.pm.pip.search.MemGraphSearch;
import gov.nist.csd.pm.pip.search.Neo4jSearch;
import gov.nist.csd.pm.utils.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static gov.nist.csd.pm.graph.model.nodes.NodeType.OA;
import static gov.nist.csd.pm.graph.model.nodes.NodeType.PC;
import static gov.nist.csd.pm.graph.model.nodes.NodeType.UA;
import static org.junit.jupiter.api.Assertions.*;

public class MemGraphIT {

    private static Graph  loadedGraph;
    private static String testID;
    private static long   pc1ID;
    private static long   oa1ID;
    private static long   ua1ID;
    private static long   o1ID;
    private static long   u1ID;

    @BeforeAll
    public static void setUp() throws PMException, IOException {
        // set up neo4j graph
        Neo4jGraph neoGraph = new Neo4jGraph(TestUtils.getDatabaseContext());
        testID = UUID.randomUUID().toString();

        u1ID = neoGraph.createNode(new Node(5, "u1", NodeType.U, NodeUtils.toProperties("namespace", testID)));
        o1ID = neoGraph.createNode(new Node(3, "o1", NodeType.O, NodeUtils.toProperties("namespace", testID)));

        ua1ID = neoGraph.createNode(new Node(4, "ua1", UA, NodeUtils.toProperties("namespace", testID)));
        neoGraph.assign(new Node(u1ID, NodeType.U), new Node(ua1ID, UA));

        oa1ID = neoGraph.createNode(new Node(2, "oa1", OA, NodeUtils.toProperties("namespace", testID)));
        neoGraph.assign(new Node(o1ID, NodeType.O), new Node(oa1ID, OA));

        pc1ID = neoGraph.createNode(new Node(1, "pc1", PC, NodeUtils.toProperties("namespace", testID)));
        neoGraph.assign(new Node(ua1ID, UA), new Node(pc1ID, PC));
        neoGraph.assign(new Node(oa1ID, OA), new Node(pc1ID, PC));

        neoGraph.associate(new Node(ua1ID, UA), new Node(oa1ID, OA), new HashSet<>(Arrays.asList("read", "write")));

        loadedGraph = new MemGraph();
        Neo4jGraphLoader loader = new Neo4jGraphLoader(TestUtils.getDatabaseContext());
        Set<Node> nodes = loader.getNodes();
        for(Node node : nodes) {
            loadedGraph.createNode(node);
        }

        Set<Assignment> assignments = loader.getAssignments();
        for(Assignment assignment : assignments) {
            long childID = assignment.getSourceID();
            long parentID = assignment.getTargetID();
            loadedGraph.assign(loadedGraph.getNode(childID), loadedGraph.getNode(parentID));
        }

        Set<Association> associations = loader.getAssociations();
        for(Association association : associations) {
            long uaID = association.getSourceID();
            long targetID = association.getTargetID();
            Set<String> operations = association.getOperations();
            loadedGraph.associate(loadedGraph.getNode(uaID), loadedGraph.getNode(targetID), operations);
        }
    }

    @AfterAll
    public static void tearDown() throws PMException, IOException {
        Neo4jGraph neoGraph = new Neo4jGraph(TestUtils.getDatabaseContext());
        Set<Node> nodes = new Neo4jSearch(TestUtils.getDatabaseContext()).search(null, null, NodeUtils.toProperties("namespace", testID));
        for(Node node : nodes) {
            neoGraph.deleteNode(node.getID());
        }
    }

    @Test
    public void testCreateNode() throws PMException {
        MemGraph graph = new MemGraph();
        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> graph.createNode(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.createNode(new Node())),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.createNode(new Node(123, null, OA, null))),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.createNode(new Node(123, "", OA, null))),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.createNode(new Node(123, "name", null, null)))
        );

        // add pc
        long pc = graph.createNode(new Node(123, "pc", PC, null));
        assertTrue(graph.getPolicies().contains(pc));

        // add non pc
        long nodeID = graph.createNode(new Node(1234, "oa", OA, NodeUtils.toProperties("namespace", "test")));

        // check node is added
        MemGraphSearch search = new MemGraphSearch(graph);
        Node node = search.getNode(nodeID);
        assertEquals("oa", node.getName());
        assertEquals(OA, node.getType());
    }

    @Test
    public void testUpdateNode() throws PMException {
        MemGraph graph = new MemGraph();
        Node node = new Node(123, "node", OA, NodeUtils.toProperties("namespace", "test"));
        long nodeID = graph.createNode(node);

        // node not found
        assertThrows(PMException.class, () -> graph.updateNode(new Node(9, "newNodeName", null, null)));

        // update name
        graph.updateNode(node.name("updated name"));
        assertEquals(graph.getNode(nodeID).getName(), "updated name");

        // update properties
        graph.updateNode(node.property("newKey", "newValue"));
        assertEquals(graph.getNode(nodeID).getProperties().get("newKey"), "newValue");
    }

    @Test
    public void testDeleteNode() throws PMException {
        MemGraph graph = new MemGraph();
        long id = graph.createNode(new Node(123, "node", PC, NodeUtils.toProperties("namespace", "test")));

        graph.deleteNode(id);

        // deleted from the graph
        assertFalse(graph.exists(id));

        // deleted from list of policies
        assertFalse(graph.getPolicies().contains(id));
    }

    @Test
    public void testExists() throws PMException {
        Graph graph = new MemGraph();
        long id = graph.createNode(new Node(123, "node", OA, null));
        assertTrue(graph.exists(id));
        assertFalse(graph.exists(1234));

        // test the loaded graph
        assertTrue(loadedGraph.exists(pc1ID));
        assertTrue(loadedGraph.exists(oa1ID));
        assertTrue(loadedGraph.exists(ua1ID));
        assertTrue(loadedGraph.exists(u1ID));
        assertTrue(loadedGraph.exists(o1ID));
    }

    @Test
    public void testGetNodes() throws PMException {
        Graph graph = new MemGraph();

        assertTrue(graph.getNodes().isEmpty());

        graph.createNode(new Node(123, "node1", OA, null));
        graph.createNode(new Node(1234, "node2", OA, null));
        graph.createNode(new Node(1235, "node3", OA, null));

        assertEquals(3, graph.getNodes().size());

        // laoded graph
        Collection<Node> nodes = loadedGraph.getNodes();
        assertTrue(nodes.containsAll(Arrays.asList(
                new Node().id(pc1ID),
                new Node().id(oa1ID),
                new Node().id(ua1ID),
                new Node().id(u1ID),
                new Node().id(o1ID)
        )));
    }

    @Test
    public void testGetPolicies() throws PMException {
        Graph graph = new MemGraph();

        assertTrue(graph.getPolicies().isEmpty());

        graph.createNode(new Node(123, "node1", PC, null));
        graph.createNode(new Node(1234, "node2", PC, null));
        graph.createNode(new Node(1235, "node3", PC, null));

        assertEquals(3, graph.getPolicies().size());

        // loaded graph
        assertTrue(loadedGraph.getPolicies().contains(pc1ID));
    }

    @Test
    public void testGetChildren() throws PMException {
        Graph graph = new MemGraph();

        assertThrows(PMException.class, () -> graph.getChildren(1));

        long parentID = graph.createNode(new Node(1, "parent", OA, null));
        long child1ID = graph.createNode(new Node(2, "child1", OA, null));
        long child2ID = graph.createNode(new Node(3, "child2", OA, null));

        graph.assign(new Node(child1ID, OA), new Node(parentID, OA));
        graph.assign(new Node(child2ID, OA), new Node(parentID, OA));

        Set<Long> children = graph.getChildren(parentID);
        assertTrue(children.containsAll(Arrays.asList(child1ID, child2ID)));

        // loaded graph
        children = loadedGraph.getChildren(pc1ID);
        assertTrue(children.containsAll(Arrays.asList(oa1ID, ua1ID)));

        children = loadedGraph.getChildren(oa1ID);
        assertTrue(children.contains(o1ID));

        children = loadedGraph.getChildren(ua1ID);
        assertTrue(children.contains(u1ID));
    }

    @Test
    public void testGetParents() throws PMException {
        Graph graph = new MemGraph();

        assertThrows(PMException.class, () -> graph.getChildren(1));

        long parent1ID = graph.createNode(new Node(1, "parent1", OA, null));
        long parent2ID = graph.createNode(new Node(2, "parent2", OA, null));
        long child1ID = graph.createNode(new Node(3, "child1", OA, null));

        graph.assign(new Node(child1ID, OA), new Node(parent1ID, OA));
        graph.assign(new Node(child1ID, OA), new Node(parent2ID, OA));

        Set<Long> parents = graph.getParents(child1ID);
        assertTrue(parents.contains(parent1ID));
        assertTrue(parents.contains(parent2ID));

        // loaded graph
        parents = loadedGraph.getParents(oa1ID);
        assertTrue(parents.contains(pc1ID));
        parents = loadedGraph.getParents(ua1ID);
        assertTrue(parents.contains(pc1ID));
        parents = loadedGraph.getParents(o1ID);
        assertTrue(parents.contains(oa1ID));
        parents = loadedGraph.getParents(u1ID);
        assertTrue(parents.contains(ua1ID));
    }

    @Test
    public void testAssign() throws PMException {
        Graph graph = new MemGraph();

        long parent1ID = graph.createNode(new Node(1, "parent1", OA, null));
        long child1ID = graph.createNode(new Node(3, "child1", OA, null));

        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> graph.assign(null, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.assign(new Node(), null)),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.assign(new Node().id(123), null)),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.assign(new Node().id(1), new Node().id(123)))
        );

        graph.assign(new Node(child1ID, OA), new Node(parent1ID, OA));

        assertTrue(graph.getChildren(parent1ID).contains(child1ID));
        assertTrue(graph.getParents(child1ID).contains(parent1ID));
    }

    @Test
    public void testDeassign() throws PMException {
        Graph graph = new MemGraph();

        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> graph.assign(null, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> graph.assign(new Node(), null))
        );

        long parent1ID = graph.createNode(new Node(1, "parent1", OA, null));
        long child1ID = graph.createNode(new Node(3, "child1", OA, null));

        graph.assign(new Node(child1ID, OA), new Node(parent1ID, OA));
        graph.deassign(new Node(child1ID, OA), new Node(parent1ID, OA));

        assertFalse(graph.getChildren(parent1ID).contains(new Node().id(child1ID)));
        assertFalse(graph.getParents(child1ID).contains(new Node().id(parent1ID)));
    }

    @Test
    public void testAssociate() throws PMException {
        Graph graph = new MemGraph();

        long uaID = graph.createNode(new Node(1, "ua", UA, null));
        long targetID = graph.createNode(new Node(3, "target", OA, null));

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
        Graph graph = new MemGraph();

        long uaID = graph.createNode(new Node(1, "ua", UA, null));
        long targetID = graph.createNode(new Node(3, "target", OA, null));

        graph.associate(new Node(uaID, UA), new Node(targetID, OA), new HashSet<>(Arrays.asList("read", "write")));
        graph.dissociate(new Node(uaID, UA), new Node(targetID, OA));

        Map<Long, Set<String>> associations = graph.getSourceAssociations(uaID);
        assertFalse(associations.containsKey(targetID));

        associations = graph.getTargetAssociations(targetID);
        assertFalse(associations.containsKey(uaID));
    }

    @Test
    public void testGetSourceAssociations() throws PMException {
        Graph graph = new MemGraph();

        long uaID = graph.createNode(new Node(1, "ua", UA, null));
        long targetID = graph.createNode(new Node(3, "target", OA, null));

        graph.associate(new Node(uaID, UA), new Node(targetID, OA), new HashSet<>(Arrays.asList("read", "write")));

        Map<Long, Set<String>> associations = graph.getSourceAssociations(uaID);
        assertTrue(associations.containsKey(targetID));
        assertTrue(associations.get(targetID).containsAll(Arrays.asList("read", "write")));

        assertThrows(PMException.class, () -> graph.getSourceAssociations(123));

        // loaded graph
        associations = loadedGraph.getSourceAssociations(ua1ID);
        assertTrue(associations.containsKey(oa1ID));
        assertTrue(associations.get(oa1ID).containsAll(Arrays.asList("read", "write")));
    }

    @Test
    public void testGetTargetAssociations() throws PMException {
        Graph graph = new MemGraph();

        long uaID = graph.createNode(new Node(1, "ua", UA, null));
        long targetID = graph.createNode(new Node(3, "target", OA, null));

        graph.associate(new Node(uaID, UA), new Node(targetID, OA), new HashSet<>(Arrays.asList("read", "write")));

        Map<Long, Set<String>> associations = graph.getTargetAssociations(targetID);
        assertTrue(associations.containsKey(uaID));
        assertTrue(associations.get(uaID).containsAll(Arrays.asList("read", "write")));

        assertThrows(PMException.class, () -> graph.getTargetAssociations(123));

        // loaded graph
        associations = loadedGraph.getTargetAssociations(oa1ID);
        assertTrue(associations.containsKey(ua1ID));
        assertTrue(associations.get(ua1ID).containsAll(Arrays.asList("read", "write")));
    }
}