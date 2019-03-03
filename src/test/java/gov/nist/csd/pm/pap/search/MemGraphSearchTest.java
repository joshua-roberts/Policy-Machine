package gov.nist.csd.pm.pap.search;

import gov.nist.csd.pm.common.util.NodeUtils;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.MemGraph;
import gov.nist.csd.pm.graph.model.nodes.Node;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static gov.nist.csd.pm.graph.model.nodes.NodeType.OA;
import static org.junit.jupiter.api.Assertions.*;

class MemGraphSearchTest {

    @Test
    void testSearch() throws PMException {
        MemGraph graph = new MemGraph();
        Search search = new MemGraphSearch(graph);

        graph.createNode(new Node(1, "oa1", OA, NodeUtils.toProperties("namespace", "test")));
        graph.createNode(new Node(2, "oa2", OA, NodeUtils.toProperties("key1", "value1")));
        graph.createNode(new Node(3, "oa3", OA, NodeUtils.toProperties("key1", "value1", "key2", "value2")));

        // name and type no properties
        Set<Node> nodes = search.search("oa1", OA.toString(), null);
        assertEquals(1, nodes.size());

        // one property
        nodes = search.search(null, null, NodeUtils.toProperties("key1", "value1"));
        assertEquals(2, nodes.size());

        // just namespace
        nodes = search.search(null, null, NodeUtils.toProperties("namespace", "test"));
        assertEquals(1, nodes.size());

        // name, type, namespace
        nodes = search.search("oa1", OA.toString(), NodeUtils.toProperties("namespace", "test"));
        assertEquals(1, nodes.size());

        nodes = search.search(null, OA.toString(), NodeUtils.toProperties("namespace", "test"));
        assertEquals(1, nodes.size());
        nodes = search.search(null, OA.toString(), null);
        assertEquals(3, nodes.size());
        nodes = search.search(null, OA.toString(), NodeUtils.toProperties("key1", "value1"));
        assertEquals(2, nodes.size());
        nodes = search.search(null, null, null);
        assertEquals(3, nodes.size());
    }

    @Test
    void testGetNode() throws PMException {
        MemGraph graph = new MemGraph();
        Search search = new MemGraphSearch(graph);

        assertThrows(PMException.class, () -> search.getNode(123));

        long id = graph.createNode(new Node(123, "oa1", OA, null));
        Node node = search.getNode(id);
        assertEquals("oa1", node.getName());
        assertEquals(OA, node.getType());
    }
}