package gov.nist.csd.pm.pip.graph;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Search;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pip.loader.DummyLoader;
import gov.nist.csd.pm.pip.loader.LoaderException;
import gov.nist.csd.pm.pip.model.DatabaseContext;
import gov.nist.csd.pm.pip.search.Neo4jSearch;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NGACNeo4jTest {

    private NGACNeo4j neo;
    private String uuid;
    private DatabaseContext ctx = new DatabaseContext("localhost", 7687, "neo4j", "root", "");

    @BeforeEach
    void setUp() {
        try {
            neo = new NGACNeo4j(ctx);

            uuid = UUID.randomUUID().toString();
            HashMap<String, String> properties = new HashMap<>();
            properties.put("uui", uuid);

            //create nodes
            Node pc1 = neo.createNode(new Node(hashID("pc1", NodeType.PC, properties), "pc1", NodeType.PC, properties));
            Node oa1 = neo.createNode(new Node(hashID("oa1", NodeType.OA, properties), "oa1", NodeType.OA, properties));
            Node o1 = neo.createNode(new Node(hashID("o1", NodeType.O, properties), "o1", NodeType.O, properties));
            Node ua1 = neo.createNode(new Node(hashID("ua1", NodeType.UA, properties), "ua1", NodeType.UA, properties));
            Node u1 = neo.createNode(new Node(hashID("u1", NodeType.U, properties), "u1", NodeType.U, properties));

            //create assignments
            neo.assign(oa1, pc1);
            neo.assign(o1, oa1);
            neo.assign(ua1, pc1);
            neo.assign(u1, ua1);

            //create associations
            neo.associate(ua1.getID(), oa1.getID(), new HashSet<>(Arrays.asList("read", "write")));
        }
        catch (NullNodeCtxException | DatabaseException | NullTypeException e) {
            fail(e.getMessage());
        }
    }

    private static long hashID(String name, NodeType type, HashMap<String, String> properties) {
        long propsHash = 0;
        for (String key : properties.keySet()) {
            propsHash ^= key.hashCode() ^ properties.get(key).hashCode();
        }
        return name.hashCode() ^ type.hashCode() ^ propsHash;
    }

    @AfterEach
    void tearDown() {
        try {
            Search search = new Neo4jSearch(ctx);
            HashMap<String, String> properties = new HashMap<>();
            properties.put("uui", uuid);
            HashSet<Node> results = search.search(null, null, properties);
            for(Node node : results) {
                neo.deleteNode(node.getID());
            }
        }
        catch (DatabaseException | SessionDoesNotExistException | LoadConfigException | LoaderException | MissingPermissionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void test() {
        try {
            System.out.println(neo.getNodes());
        }
        catch (DatabaseException e) {
            fail(e.getMessage());
        }
    }

    /*@Nested
    @DisplayName("tests for createNode")
    class CreateNodeTests {

        @Test
        @DisplayName("test create node")
        void test1() {
            try {
                Node test_oa = neo.createNode(new Node(6, "test_oa", NodeType.OA, null));
                assertTrue(neo.exists(test_oa.getID()));
            }
            catch (NoIDException | NullNodeCtxException e) {
                fail(e.getMessage());
            }
        }

        @Test
        @DisplayName("test create node NoIDException")
        void test2() {
            Assertions.assertThrows(NoIDException.class, () -> neo.createNode(new Node("test_oa", NodeType.OA, null)));
        }

        @Test
        @DisplayName("test create node NullNodeCtxException")
        void test3() {
            Assertions.assertThrows(NullNodeCtxException.class, () -> neo.createNode(null));
        }
    }

    *//**
     * This test is empty because update node has no affect on the in-memory graph.
     *//*
    @Test
    @DisplayName("tests for updateNode")
    void testUpdateNode() {
    }

    @Nested
    @DisplayName("tests for deleteNode")
    class DeleteNodeTests {

        @Test
        @DisplayName("test delete pc deletes from graph and pcs")
        void test1() {
            neo.deleteNode(1);

            assertFalse(neo.exists(1));
            assertFalse(neo.getPolicies().contains(1L));
        }
    }

    @Nested
    @DisplayName("tests for exists")
    class ExistsTests {

        @Test
        @DisplayName("test exists for existing node")
        void test1() {
            assertTrue(neo.exists(1));
        }

        @Test
        @DisplayName("test exists for non existing node")
        void test2() {
            assertFalse(neo.exists(0));
        }
    }

    @Nested
    @DisplayName("tests for getNodes")
    class GetNodesTests {

        @Test
        @DisplayName("test getNodes")
        void test1() {
            HashSet<Node> nodes = neo.getNodes();
            assertEquals(5, nodes.size());
        }
    }

    @Nested
    @DisplayName("tests for getChildren")
    class GetChildrenTests {

        @Test
        @DisplayName("test getChildren")
        void test1() {
            HashSet<Node> children = neo.getChildren(2);
            assertEquals(1, children.size());
            // check that the IDs match
            assertEquals(3, children.iterator().next().getID());
        }
    }

    @Nested
    @DisplayName("tests for getParents")
    class GetParentsTests {

        @Test
        @DisplayName("test getParents")
        void test1() {
            HashSet<Node> parents = neo.getParents(2);
            assertEquals(1, parents.size());
            // check that the IDs match
            assertEquals(1, parents.iterator().next().getID());
        }
    }

    @Nested
    @DisplayName("tests for assign")
    class AssignTests {

        @Test
        @DisplayName("test assign")
        void test1() {
            try {
                Node testOA1 = neo.createNode(new Node(6, "test_oa1", NodeType.OA, null));
                Node testOA2 = neo.createNode(new Node(7, "test_oa2", NodeType.OA, null));
                neo.assign(testOA1, testOA2);
                //check that the assignment was made
                HashSet<Node> children = neo.getChildren(testOA2.getID());
                assertEquals(1, children.size());
                assertEquals(6, children.iterator().next().getID());
            }
            catch (NoIDException | NullNodeCtxException e) {
                fail(e.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("tests for deassign")
    class DeassignTests {

        @Test
        @DisplayName("test deassign")
        void test1() {
            try {
                neo.deassign(new Node().id(3), new Node().id(2));
                //check that the assignment was deleted
                HashSet<Node> children = neo.getChildren(2);
                assertEquals(0, children.size());
            }
            catch (NullNodeCtxException e) {
                fail(e.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("tests for associate")
    class AssociateTests {

        @Test
        @DisplayName("test associate")
        void test1() {
            try {
                neo.createNode(new Node(6, "test_oa1", NodeType.UA, null));
                neo.createNode(new Node(7, "test_oa2", NodeType.OA, null));
                neo.associate(6, 7, new HashSet<>(Arrays.asList("read", "write")));
                //check that the association was made for both
                assertTrue(neo.getSourceAssociations(6).containsKey(7L));
                assertTrue(neo.getTargetAssociations(7).containsKey(6L));
            }
            catch (NoIDException | NullNodeCtxException e) {
                fail(e.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("tests for dissociate")
    class DisssociateTests {

        @Test
        @DisplayName("test dissociate")
        void test1() {
            neo.dissociate(4, 2);
            //check that the association was deleted for both
            assertFalse(neo.getSourceAssociations(4).containsKey(2L));
            assertFalse(neo.getTargetAssociations(2).containsKey(4L));
        }
    }

    @Nested
    @DisplayName("tests for getSourceAssociations")
    class GetSourceAssociationsTests {

        @Test
        @DisplayName("test getSourceAssociations")
        void test1() {
            assertTrue(neo.getSourceAssociations(4).containsKey(2L));
            assertTrue(neo.getTargetAssociations(2).containsKey(4L));
        }
    }

    @Nested
    @DisplayName("tests for getTargetAssociations")
    class GetTargetAssociationsTests {

        @Test
        @DisplayName("test getTargetAssociations")
        void test1() {
            assertTrue(neo.getTargetAssociations(2).containsKey(4L));
        }
    }*/
}