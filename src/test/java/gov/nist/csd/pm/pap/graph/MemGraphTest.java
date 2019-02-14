package gov.nist.csd.pm.pap.graph;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.common.model.graph.nodes.NodeUtils;
import gov.nist.csd.pm.pap.search.MemGraphSearch;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.OA;
import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.PC;
import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.UA;
import static org.junit.jupiter.api.Assertions.*;

public class MemGraphTest {

    @Test
    public void testCreateNode() throws PMException {
        MemGraph graph = new MemGraph();
        assertAll(() -> assertThrows(PMException.class, () -> graph.createNode(null)),
                () -> assertThrows(PMException.class, () -> graph.createNode(new NodeContext())),
                () -> assertThrows(PMException.class, () -> graph.createNode(new NodeContext(123, null, OA, null))),
                () -> assertThrows(PMException.class, () -> graph.createNode(new NodeContext(123, "", OA, null))),
                () -> assertThrows(PMException.class, () -> graph.createNode(new NodeContext(123, "name", null, null)))
        );

        // add pc
        long pc = graph.createNode(new NodeContext(123, "pc", PC, null));
        assertTrue(graph.getPolicies().contains(pc));

        // add non pc
        long nodeID = graph.createNode(new NodeContext(1234, "oa", OA, NodeUtils.toProperties("namespace", "test")));

        // check namespace is added
        HashMap<String, Long> namespaceNames = graph.getNamespaceNames();
        assertEquals((long) namespaceNames.get("test:oa:OA"), nodeID);

        // check node is added
        MemGraphSearch search = new MemGraphSearch(graph);
        NodeContext node = search.getNode(nodeID);
        assertEquals("oa", node.getName());
        assertEquals(OA, node.getType());
    }

    @Test
    public void testUpdateNode() throws PMException {
        MemGraph graph = new MemGraph();
        NodeContext node = new NodeContext(123, "node", OA, NodeUtils.toProperties("namespace", "test"));
        long nodeID = graph.createNode(node);

        // node not found
        assertThrows(PMException.class, () -> graph.updateNode(new NodeContext(9, "newNodeName", null, null)));

        // update name
        graph.updateNode(node.name("updated name"));
        assertEquals(graph.getNodesMap().get(nodeID).getName(), "updated name");

        // update properties
        graph.updateNode(node.property("newKey", "newValue"));
        assertEquals(graph.getNodesMap().get(nodeID).getProperties().get("newKey"), "newValue");

        // check that the namespace tuple updated
        assertEquals(graph.getNamespaceNames().get("test:updated name:OA").longValue(), nodeID);
    }

    @Test
    public void testDeleteNode() throws PMException {
        MemGraph graph = new MemGraph();
        long id = graph.createNode(new NodeContext(123, "node", PC, NodeUtils.toProperties("namespace", "test")));

        graph.deleteNode(id);

        // deleted from the graph
        assertFalse(graph.exists(id));

        // deleted from the node map
        assertFalse(graph.getNodesMap().containsKey(id));

        // deleted from list of policies
        assertFalse(graph.getPolicies().contains(id));

        // namesapce tuple removed
        assertFalse(graph.getNamespaceNames().containsKey("test:node:PC"));
    }

    @Test
    public void testExists() throws PMException {
        Graph graph = new MemGraph();
        long id = graph.createNode(new NodeContext(123, "node", OA, null));
        assertTrue(graph.exists(id));
        assertFalse(graph.exists(1234));
    }

    @Test
    public void testGetNodes() throws PMException {
        Graph graph = new MemGraph();

        assertTrue(graph.getNodes().isEmpty());

        graph.createNode(new NodeContext(123, "node1", OA, null));
        graph.createNode(new NodeContext(1234, "node2", OA, null));
        graph.createNode(new NodeContext(1235, "node3", OA, null));

        assertEquals(3, graph.getNodes().size());
    }

    @Test
    public void testGetPolicies() throws PMException {
        Graph graph = new MemGraph();

        assertTrue(graph.getPolicies().isEmpty());

        graph.createNode(new NodeContext(123, "node1", PC, null));
        graph.createNode(new NodeContext(1234, "node2", PC, null));
        graph.createNode(new NodeContext(1235, "node3", PC, null));

        assertEquals(3, graph.getPolicies().size());
    }

    @Test
    public void testGetChildren() throws PMException {
        Graph graph = new MemGraph();

        assertThrows(PMException.class, () -> graph.getChildren(1));

        long parentID = graph.createNode(new NodeContext(1, "parent", OA, null));
        long child1ID = graph.createNode(new NodeContext(2, "child1", OA, null));
        long child2ID = graph.createNode(new NodeContext(3, "child2", OA, null));

        graph.assign(new NodeContext(child1ID, OA), new NodeContext(parentID, OA));
        graph.assign(new NodeContext(child2ID, OA), new NodeContext(parentID, OA));

        HashSet<NodeContext> children = graph.getChildren(parentID);
        assertTrue(children.contains(new NodeContext().id(child1ID)));
        assertTrue(children.contains(new NodeContext().id(child2ID)));
    }

    @Test
    public void testGetParents() throws PMException {
        Graph graph = new MemGraph();

        assertThrows(PMException.class, () -> graph.getChildren(1));

        long parent1ID = graph.createNode(new NodeContext(1, "parent1", OA, null));
        long parent2ID = graph.createNode(new NodeContext(2, "parent2", OA, null));
        long child1ID = graph.createNode(new NodeContext(3, "child1", OA, null));

        graph.assign(new NodeContext(child1ID, OA), new NodeContext(parent1ID, OA));
        graph.assign(new NodeContext(child1ID, OA), new NodeContext(parent2ID, OA));

        HashSet<NodeContext> children = graph.getParents(child1ID);
        assertTrue(children.contains(new NodeContext().id(parent1ID)));
        assertTrue(children.contains(new NodeContext().id(parent2ID)));
    }

    @Test
    public void testAssign() throws PMException {
        Graph graph = new MemGraph();

        long parent1ID = graph.createNode(new NodeContext(1, "parent1", OA, null));
        long child1ID = graph.createNode(new NodeContext(3, "child1", OA, null));

        assertAll(() -> assertThrows(PMException.class, () -> graph.assign(null, null)),
                () -> assertThrows(PMException.class, () -> graph.assign(new NodeContext(), null)),
                () -> assertThrows(PMException.class, () -> graph.assign(new NodeContext().id(123), null)),
                () -> assertThrows(PMException.class, () -> graph.assign(new NodeContext().id(1), new NodeContext().id(123)))
        );

        graph.assign(new NodeContext(child1ID, OA), new NodeContext(parent1ID, OA));

        assertTrue(graph.getChildren(parent1ID).contains(new NodeContext().id(child1ID)));
        assertTrue(graph.getParents(child1ID).contains(new NodeContext().id(parent1ID)));
    }

    @Test
    public void testDeassign() throws PMException {
        Graph graph = new MemGraph();

        assertAll(() -> assertThrows(PMException.class, () -> graph.assign(null, null)),
                () -> assertThrows(PMException.class, () -> graph.assign(new NodeContext(), null))
        );

        long parent1ID = graph.createNode(new NodeContext(1, "parent1", OA, null));
        long child1ID = graph.createNode(new NodeContext(3, "child1", OA, null));

        graph.assign(new NodeContext(child1ID, OA), new NodeContext(parent1ID, OA));
        graph.deassign(new NodeContext(child1ID, OA), new NodeContext(parent1ID, OA));

        assertFalse(graph.getChildren(parent1ID).contains(new NodeContext().id(child1ID)));
        assertFalse(graph.getParents(child1ID).contains(new NodeContext().id(parent1ID)));
    }

    @Test
    public void testAssociate() throws PMException {
        Graph graph = new MemGraph();

        long uaID = graph.createNode(new NodeContext(1, "ua", UA, null));
        long targetID = graph.createNode(new NodeContext(3, "target", OA, null));

        graph.associate(new NodeContext(uaID, UA), new NodeContext(targetID, OA), new HashSet<>(Arrays.asList("read", "write")));

        HashMap<Long, HashSet<String>> associations = graph.getSourceAssociations(uaID);
        assertTrue(associations.containsKey(targetID));
        assertTrue(associations.get(targetID).containsAll(Arrays.asList("read", "write")));

        associations = graph.getTargetAssociations(targetID);
        assertTrue(associations.containsKey(uaID));
        assertTrue(associations.get(uaID).containsAll(Arrays.asList("read", "write")));
    }

    @Test
    public void testDissociate() throws PMException {
        Graph graph = new MemGraph();

        long uaID = graph.createNode(new NodeContext(1, "ua", UA, null));
        long targetID = graph.createNode(new NodeContext(3, "target", OA, null));

        graph.associate(new NodeContext(uaID, UA), new NodeContext(targetID, OA), new HashSet<>(Arrays.asList("read", "write")));
        graph.dissociate(new NodeContext(uaID, UA), new NodeContext(targetID, OA));

        HashMap<Long, HashSet<String>> associations = graph.getSourceAssociations(uaID);
        assertFalse(associations.containsKey(targetID));

        associations = graph.getTargetAssociations(targetID);
        assertFalse(associations.containsKey(uaID));
    }

    @Test
    public void testGetSourceAssociations() throws PMException {
        Graph graph = new MemGraph();

        long uaID = graph.createNode(new NodeContext(1, "ua", UA, null));
        long targetID = graph.createNode(new NodeContext(3, "target", OA, null));

        graph.associate(new NodeContext(uaID, UA), new NodeContext(targetID, OA), new HashSet<>(Arrays.asList("read", "write")));
        graph.dissociate(new NodeContext(uaID, UA), new NodeContext(targetID, OA));

        HashMap<Long, HashSet<String>> associations = graph.getSourceAssociations(uaID);
        assertFalse(associations.containsKey(targetID));

        assertThrows(PMException.class, () -> graph.getSourceAssociations(123));
    }

    @Test
    public void testGetTargetAssociations() throws PMException {
        Graph graph = new MemGraph();

        long uaID = graph.createNode(new NodeContext(1, "ua", UA, null));
        long targetID = graph.createNode(new NodeContext(3, "target", OA, null));

        graph.associate(new NodeContext(uaID, UA), new NodeContext(targetID, OA), new HashSet<>(Arrays.asList("read", "write")));
        graph.dissociate(new NodeContext(uaID, UA), new NodeContext(targetID, OA));

        HashMap<Long, HashSet<String>> associations = graph.getTargetAssociations(targetID);
        assertFalse(associations.containsKey(uaID));

        assertThrows(PMException.class, () -> graph.getTargetAssociations(123));
    }
}