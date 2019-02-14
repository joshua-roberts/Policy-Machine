package gov.nist.csd.pm.pap.graph;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.common.model.graph.nodes.NodeUtils;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.search.MemGraphSearch;
import gov.nist.csd.pm.pap.search.Neo4jSearch;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.OA;
import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.PC;
import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.UA;
import static gov.nist.csd.pm.pap.PAP.getPAP;
import static org.junit.jupiter.api.Assertions.*;

public class Neo4jGraphIT {

    private Neo4jGraph graph;
    private Neo4jSearch search;
    private String testID;
    private DatabaseContext dbCtx;


    @BeforeEach
    public void setUp() throws PMException {
        dbCtx = new DatabaseContext("localhost", 7687, "neo4j", "root", null);
        graph = new Neo4jGraph(dbCtx);
        search = new Neo4jSearch(dbCtx);
        testID = UUID.randomUUID().toString();
    }

    @AfterEach
    public void tearDown() throws PMException {
        HashSet<NodeContext> nodes = new Neo4jSearch(dbCtx).search(null, null, NodeUtils.toProperties("namespace", testID));
        for(NodeContext node : nodes) {
            graph.deleteNode(node.getID());
        }
    }

    @Test
    public void testCreateNode() throws PMException {
        assertAll(() -> assertThrows(PMException.class, () -> graph.createNode(null)),
                () -> assertThrows(PMException.class, () -> graph.createNode(new NodeContext(null, OA, null))),
                () -> assertThrows(PMException.class, () -> graph.createNode(new NodeContext("", OA, null))),
                () -> assertThrows(PMException.class, () -> graph.createNode(new NodeContext("name", null, null)))
        );

        // add pc
        long pc = graph.createNode(new NodeContext("pc", PC, NodeUtils.toProperties("namespace", testID)));
        assertTrue(graph.getPolicies().contains(pc));

        // add non pc
        long nodeID = graph.createNode(new NodeContext("oa", OA, NodeUtils.toProperties("namespace", testID)));

        // check node is added
        NodeContext node = search.getNode(nodeID);
        assertEquals("oa", node.getName());
        assertEquals(OA, node.getType());
    }

    @Test
    public void testUpdateNode() throws PMException {
        NodeContext node = new NodeContext("node", OA, NodeUtils.toProperties("namespace", testID));
        long nodeID = graph.createNode(node);

        // node not found
        assertThrows(PMException.class, () -> graph.updateNode(new NodeContext("newNodeName", null, null)));

        // update name
        graph.updateNode(node.name("updated name").id(nodeID));
        assertEquals(search.getNode(nodeID).getName(), "updated name");

        // update properties
        graph.updateNode(node.property("newKey", "newValue"));
        assertEquals(search.getNode(nodeID).getProperties().get("newKey"), "newValue");
    }

    @Test
    public void testDeleteNode() throws PMException {
        long id = graph.createNode(new NodeContext("node", PC, NodeUtils.toProperties("namespace", testID)));

        graph.deleteNode(id);

        // deleted from the graph
        assertFalse(graph.exists(id));

        // deleted from the node map

        // deleted from list of policies
        assertFalse(graph.getPolicies().contains(id));
    }

    @Test
    public void testExists() throws PMException {
        long id = graph.createNode(new NodeContext("node", OA, NodeUtils.toProperties("namespace", testID)));
        assertTrue(graph.exists(id));
        assertFalse(graph.exists(new Random().nextLong()));
    }

    @Test
    public void testGetNodes() throws PMException {
        long node1 = graph.createNode(new NodeContext("node1", OA, NodeUtils.toProperties("namespace", testID)));
        long node2 = graph.createNode(new NodeContext("node2", OA, NodeUtils.toProperties("namespace", testID)));
        long node3 = graph.createNode(new NodeContext("node3", OA, NodeUtils.toProperties("namespace", testID)));

        assertTrue(graph.getNodes().containsAll(Arrays.asList(new NodeContext().id(node1), new NodeContext().id(node2), new NodeContext().id(node3))));
    }

    @Test
    public void testGetPolicies() throws PMException {
        long node1 = graph.createNode(new NodeContext("node1", PC, NodeUtils.toProperties("namespace", testID)));
        long node2 = graph.createNode(new NodeContext("node2", PC, NodeUtils.toProperties("namespace", testID)));
        long node3 = graph.createNode(new NodeContext("node3", PC, NodeUtils.toProperties("namespace", testID)));

        assertTrue(graph.getPolicies().containsAll(Arrays.asList(node1, node2, node3)));
    }

    @Test
    public void testGetChildren() throws PMException {
        long parentID = graph.createNode(new NodeContext("parent", OA, NodeUtils.toProperties("namespace", testID)));
        long child1ID = graph.createNode(new NodeContext("child1", OA, NodeUtils.toProperties("namespace", testID)));
        long child2ID = graph.createNode(new NodeContext("child2", OA, NodeUtils.toProperties("namespace", testID)));

        graph.assign(new NodeContext(child1ID, OA), new NodeContext(parentID, OA));
        graph.assign(new NodeContext(child2ID, OA), new NodeContext(parentID, OA));

        HashSet<NodeContext> children = graph.getChildren(parentID);
        assertTrue(children.contains(new NodeContext().id(child1ID)));
        assertTrue(children.contains(new NodeContext().id(child2ID)));
    }

    @Test
    public void testGetParents() throws PMException {
        long parent1ID = graph.createNode(new NodeContext("parent1", OA, NodeUtils.toProperties("namespace", testID)));
        long parent2ID = graph.createNode(new NodeContext("parent2", OA, NodeUtils.toProperties("namespace", testID)));
        long child1ID = graph.createNode(new NodeContext("child1", OA, NodeUtils.toProperties("namespace", testID)));

        graph.assign(new NodeContext(child1ID, OA), new NodeContext(parent1ID, OA));
        graph.assign(new NodeContext(child1ID, OA), new NodeContext(parent2ID, OA));

        HashSet<NodeContext> children = graph.getParents(child1ID);
        assertTrue(children.contains(new NodeContext().id(parent1ID)));
        assertTrue(children.contains(new NodeContext().id(parent2ID)));
    }

    @Test
    public void testAssign() throws PMException {
        long parent1ID = graph.createNode(new NodeContext("parent1", OA, NodeUtils.toProperties("namespace", testID)));
        long child1ID = graph.createNode(new NodeContext("child1", OA, NodeUtils.toProperties("namespace", testID)));

        assertAll(() -> assertThrows(PMException.class, () -> graph.assign(null, null)),
                () -> assertThrows(PMException.class, () -> graph.assign(new NodeContext(), null)),
                () -> assertThrows(PMException.class, () -> graph.assign(new NodeContext().id(new Random().nextLong()), null)),
                () -> assertThrows(PMException.class, () -> graph.assign(new NodeContext().id(child1ID), new NodeContext().id(new Random().nextLong())))
        );

        graph.assign(new NodeContext(child1ID, OA), new NodeContext(parent1ID, OA));

        assertTrue(graph.getChildren(parent1ID).contains(new NodeContext().id(child1ID)));
        assertTrue(graph.getParents(child1ID).contains(new NodeContext().id(parent1ID)));
    }

    @Test
    public void testDeassign() throws PMException {
        assertAll(() -> assertThrows(PMException.class, () -> graph.assign(null, null)),
                () -> assertThrows(PMException.class, () -> graph.assign(new NodeContext(), null))
        );

        long parent1ID = graph.createNode(new NodeContext("parent1", OA, NodeUtils.toProperties("namespace", testID)));
        long child1ID = graph.createNode(new NodeContext("child1", OA, NodeUtils.toProperties("namespace", testID)));

        graph.assign(new NodeContext(child1ID, OA), new NodeContext(parent1ID, OA));
        graph.deassign(new NodeContext(child1ID, OA), new NodeContext(parent1ID, OA));

        assertFalse(graph.getChildren(parent1ID).contains(new NodeContext().id(child1ID)));
        assertFalse(graph.getParents(child1ID).contains(new NodeContext().id(parent1ID)));
    }

    @Test
    public void testAssociate() throws PMException {
        long uaID = graph.createNode(new NodeContext("ua", UA, NodeUtils.toProperties("namespace", testID)));
        long targetID = graph.createNode(new NodeContext("target", OA, NodeUtils.toProperties("namespace", testID)));

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
        long uaID = graph.createNode(new NodeContext(1, "ua", UA, NodeUtils.toProperties("namespace", testID)));
        long targetID = graph.createNode(new NodeContext(3, "target", OA, NodeUtils.toProperties("namespace", testID)));

        graph.associate(new NodeContext(uaID, UA), new NodeContext(targetID, OA), new HashSet<>(Arrays.asList("read", "write")));
        graph.dissociate(new NodeContext(uaID, UA), new NodeContext(targetID, OA));

        HashMap<Long, HashSet<String>> associations = graph.getSourceAssociations(uaID);
        assertFalse(associations.containsKey(targetID));

        associations = graph.getTargetAssociations(targetID);
        assertFalse(associations.containsKey(targetID));
    }

    @Test
    public void testGetSourceAssociations() throws PMException {
        long uaID = graph.createNode(new NodeContext(1, "ua", UA, NodeUtils.toProperties("namespace", testID)));
        long targetID = graph.createNode(new NodeContext(3, "target", OA, NodeUtils.toProperties("namespace", testID)));

        graph.associate(new NodeContext(uaID, UA), new NodeContext(targetID, OA), new HashSet<>(Arrays.asList("read", "write")));
        graph.dissociate(new NodeContext(uaID, UA), new NodeContext(targetID, OA));

        HashMap<Long, HashSet<String>> associations = graph.getSourceAssociations(uaID);
        assertFalse(associations.containsKey(targetID));
    }

    @Test
    public void testGetTargetAssociations() throws PMException {
        long uaID = graph.createNode(new NodeContext(1, "ua", UA, NodeUtils.toProperties("namespace", testID)));
        long targetID = graph.createNode(new NodeContext(3, "target", OA, NodeUtils.toProperties("namespace", testID)));

        graph.associate(new NodeContext(uaID, UA), new NodeContext(targetID, OA), new HashSet<>(Arrays.asList("read", "write")));
        graph.dissociate(new NodeContext(uaID, UA), new NodeContext(targetID, OA));

        HashMap<Long, HashSet<String>> associations = graph.getTargetAssociations(targetID);
        assertFalse(associations.containsKey(uaID));
    }
}