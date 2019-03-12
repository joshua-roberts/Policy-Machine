package gov.nist.csd.pm.pip.search;

import gov.nist.csd.pm.common.util.NodeUtils;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.Neo4jGraph;
import gov.nist.csd.pm.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import static gov.nist.csd.pm.graph.model.nodes.NodeType.OA;
import static org.junit.jupiter.api.Assertions.*;

class Neo4jSearchIT {

    private Neo4jGraph graph;
    private Neo4jSearch search;
    private String testID;

    @BeforeEach
    void setUp() throws PMException, IOException {
        graph = new Neo4jGraph(TestUtils.getDatabaseContext());
        search = new Neo4jSearch(TestUtils.getDatabaseContext());
        testID = UUID.randomUUID().toString();
    }

    @AfterEach
    void tearDown() throws PMException {
        Set<Node> nodes = search.search(null, null, NodeUtils.toProperties("namespace", testID));
        for(Node node : nodes) {
            graph.deleteNode(node.getID());
        }
    }

    @Test
    void testSearch() throws PMException {
        graph.createNode(new Node("oa1", OA, NodeUtils.toProperties("namespace", testID)));
        graph.createNode(new Node("oa2", OA, NodeUtils.toProperties("namespace", testID, "key1", "value1")));
        graph.createNode(new Node("oa3", OA, NodeUtils.toProperties("namespace", testID, "key1", "value1", "key2", "value2")));

        // name and type no properties
        Set<Node> nodes = search.search("oa1", OA.toString(), NodeUtils.toProperties("namespace", testID));
        assertEquals(1, nodes.size());

        // one property
        nodes = search.search(null, null, NodeUtils.toProperties("key1", "value1"));
        assertEquals(2, nodes.size());

        // shared property
        nodes = search.search(null, null, NodeUtils.toProperties("namespace", testID));
        assertEquals(3, nodes.size());
    }

    @Test
    void testGetNode() throws PMException {
        assertThrows(PMException.class, () -> search.getNode(123));

        long id = graph.createNode(new Node("oa1", OA, NodeUtils.toProperties("namespace", testID)));
        Node node = search.getNode(id);
        assertEquals("oa1", node.getName());
        assertEquals(OA, node.getType());
        assertEquals(NodeUtils.toProperties("namespace", testID), node.getProperties());
    }
}