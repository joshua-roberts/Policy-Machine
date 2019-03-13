package gov.nist.csd.pm.pip.loader.graph;

import gov.nist.csd.pm.common.util.NodeUtils;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.graph.model.nodes.NodeType;
import gov.nist.csd.pm.graph.model.relationships.Assignment;
import gov.nist.csd.pm.graph.model.relationships.Association;
import gov.nist.csd.pm.pip.graph.Neo4jGraph;
import gov.nist.csd.pm.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class Neo4jGraphLoaderIT {

    private String     testID;
    private Neo4jGraph graph;
    private Random     random = new Random();

    private Node pc1;
    private Node oa1;
    private Node ua1;
    private Node o1;
    private Node u1;

    @BeforeEach
    void setUp() throws PMException, IOException {
        graph = new Neo4jGraph(TestUtils.getDatabaseContext());
        testID = UUID.randomUUID().toString();

        u1 = graph.createNode(random.nextLong(), "u1", NodeType.U, NodeUtils.toProperties("namespace", testID ));
        o1 = graph.createNode(random.nextLong(), "o1", NodeType.O, NodeUtils.toProperties("namespace", testID ));

        ua1 = graph.createNode(random.nextLong(), "ua1", NodeType.UA, NodeUtils.toProperties("namespace", testID ));
        graph.assign(u1.getID(), ua1.getID());

        oa1 = graph.createNode(random.nextLong(), "oa1", NodeType.OA, NodeUtils.toProperties("namespace", testID ));
        graph.assign(o1.getID(), oa1.getID());

        pc1 = graph.createNode(random.nextLong(), "pc1", NodeType.PC, NodeUtils.toProperties("namespace", testID ));
        graph.assign(ua1.getID(), pc1.getID());
        graph.assign(oa1.getID(), pc1.getID());

        graph.associate(ua1.getID(), oa1.getID(), new HashSet<>(Arrays.asList("read", "write")));
    }

    @AfterEach
    void tearDown() throws PMException {
        Set<Node> nodes = graph.search(null, null, NodeUtils.toProperties("namespace", testID));
        for(Node node : nodes) {
            graph.deleteNode(node.getID());
        }
    }

    @Test
    void testGetNodes() throws PMException, IOException {
        Neo4jGraphLoader loader = new Neo4jGraphLoader(TestUtils.getDatabaseContext());
        Set<Node> nodes = loader.getNodes();
        assertTrue(nodes.contains(pc1));
        assertTrue(nodes.contains(oa1));
        assertTrue(nodes.contains(ua1));
        assertTrue(nodes.contains(u1));
        assertTrue(nodes.contains(o1));
    }

    @Test
    void testGetAssignments() throws PMException, IOException {
        Neo4jGraphLoader loader = new Neo4jGraphLoader(TestUtils.getDatabaseContext());
        Set<Assignment> assignments = loader.getAssignments();
        assertTrue(assignments.contains(new Assignment(u1.getID(), ua1.getID())));
        assertTrue(assignments.contains(new Assignment(o1.getID(), oa1.getID())));
        assertTrue(assignments.contains(new Assignment(oa1.getID(), pc1.getID())));
        assertTrue(assignments.contains(new Assignment(ua1.getID(), pc1.getID())));
    }

    @Test
    void testGetAssociations() throws PMException, IOException {
        Neo4jGraphLoader loader = new Neo4jGraphLoader(TestUtils.getDatabaseContext());
        Set<Association> associations = loader.getAssociations();
        assertTrue(associations.contains(new Association(ua1.getID(), oa1.getID(), new HashSet<>(Arrays.asList("read", "write")))));
    }
}