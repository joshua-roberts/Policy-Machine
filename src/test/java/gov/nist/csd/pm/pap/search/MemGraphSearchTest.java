package gov.nist.csd.pm.pap.search;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.common.model.graph.nodes.NodeUtils;
import gov.nist.csd.pm.pap.graph.MemGraph;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.OA;
import static org.junit.jupiter.api.Assertions.*;

class MemGraphSearchTest {

    @Test
    void testSearch() throws PMException {
        MemGraph graph = new MemGraph();
        Search search = new MemGraphSearch(graph);

        graph.createNode(new NodeContext(1, "oa1", OA, NodeUtils.toProperties("namespace", "test")));
        graph.createNode(new NodeContext(2, "oa2", OA, NodeUtils.toProperties("key1", "value1")));
        graph.createNode(new NodeContext(3, "oa3", OA, NodeUtils.toProperties("key1", "value1", "key2", "value2")));

        // name and type no properties
        HashSet<NodeContext> nodes = search.search("oa1", OA.toString(), null);
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

        long id = graph.createNode(new NodeContext(123, "oa1", OA, null));
        NodeContext node = search.getNode(id);
        assertEquals("oa1", node.getName());
        assertEquals(OA, node.getType());
    }
}