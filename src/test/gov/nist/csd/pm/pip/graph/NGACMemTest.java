package gov.nist.csd.pm.pip.graph;

import gov.nist.csd.pm.common.exceptions.DatabaseException;
import gov.nist.csd.pm.common.exceptions.NoIDException;
import gov.nist.csd.pm.common.exceptions.NullNodeException;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pap.graph.MemGraph;
import gov.nist.csd.pm.pap.loader.graph.DummyGraphLoader;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.HashSet;

import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.*;
import static org.junit.jupiter.api.Assertions.*;

class NGACMemTest {

    MemGraph mem;

    @BeforeEach
    void setUp() {
        try {
            mem = new MemGraph(new DummyGraphLoader());

            //create nodes
            long pc1 = mem.createNode(new Node(1, "pc1", PC, null));
            long oa1 = mem.createNode(new Node(2, "oa1", NodeType.OA, null));
            long o1 = mem.createNode(new Node(3, "o1", NodeType.O, null));
            long ua1 = mem.createNode(new Node(4, "ua1", NodeType.UA, null));
            long u1 = mem.createNode(new Node(5, "u1", NodeType.U, null));

            //create assignments
            mem.assign(oa1, OA, pc1, PC);
            mem.assign(o1, O, oa1, OA);
            mem.assign(ua1, UA, pc1, PC);
            mem.assign(u1, U, ua1, UA);

            //create associations
            mem.associate(ua1, oa1, OA, new HashSet<>(Arrays.asList("read", "write")));
        }
        catch (DatabaseException | NoIDException | NullNodeException e) {
            fail(e.getMessage());
        }
    }

    @Nested
    @DisplayName("tests for createNode")
    class CreateNodeTests {

        @Test
        @DisplayName("test create node")
        void test1() {
            try {
                long test_oa = mem.createNode(new Node(6, "test_oa", NodeType.OA, null));
                assertTrue(mem.exists(test_oa));
            }
            catch (NoIDException | NullNodeException e) {
                fail(e.getMessage());
            }
        }

        @Test
        @DisplayName("test create node NoIDException")
        void test2() {
            Assertions.assertThrows(NoIDException.class, () -> mem.createNode(new Node("test_oa", NodeType.OA, null)));
        }

        @Test
        @DisplayName("test create node NullNodeCtxException")
        void test3() {
            Assertions.assertThrows(NullNodeException.class, () -> mem.createNode(null));
        }
    }

    /**
     * This test is empty because update node has no affect on the in-memory graph.
     */
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
            mem.deleteNode(1);

            assertFalse(mem.exists(1));
            assertFalse(mem.getPolicies().contains(1L));
        }
    }

    @Nested
    @DisplayName("tests for exists")
    class ExistsTests {

        @Test
        @DisplayName("test exists for existing node")
        void test1() {
            assertTrue(mem.exists(1));
        }

        @Test
        @DisplayName("test exists for non existing node")
        void test2() {
            assertFalse(mem.exists(0));
        }
    }

    @Nested
    @DisplayName("tests for getNodes")
    class GetNodesTests {

        @Test
        @DisplayName("test getNodes")
        void test1() {
            HashSet<Node> nodes = mem.getNodes();
            assertEquals(5, nodes.size());
        }
    }

    @Nested
    @DisplayName("tests for getChildren")
    class GetChildrenTests {

        @Test
        @DisplayName("test getChildren")
        void test1() {
            HashSet<Node> children = mem.getChildren(2);
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
            HashSet<Node> parents = mem.getParents(2);
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
                long testOA1 = mem.createNode(new Node(6, "test_oa1", NodeType.OA, null));
                long testOA2 = mem.createNode(new Node(7, "test_oa2", NodeType.OA, null));
                mem.assign(testOA1, OA, testOA2, OA);
                //check that the assignment was made
                HashSet<Node> children = mem.getChildren(testOA2);
                assertEquals(1, children.size());
                assertEquals(6, children.iterator().next().getID());
            }
            catch (NoIDException | NullNodeException e) {
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
            mem.deassign(3, O, 2, OA);
            //check that the assignment was deleted
            HashSet<Node> children = mem.getChildren(2);
            assertEquals(0, children.size());
        }
    }

    @Nested
    @DisplayName("tests for associate")
    class AssociateTests {

        @Test
        @DisplayName("test associate")
        void test1() {
            try {
                mem.createNode(new Node(6, "test_oa1", NodeType.UA, null));
                mem.createNode(new Node(7, "test_oa2", NodeType.OA, null));
                mem.associate(6, 7, new HashSet<>(Arrays.asList("read", "write")));
                //check that the association was made for both
                assertTrue(mem.getSourceAssociations(6).containsKey(7L));
                assertTrue(mem.getTargetAssociations(7).containsKey(6L));
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
            mem.dissociate(4, 2);
            //check that the association was deleted for both
            assertFalse(mem.getSourceAssociations(4).containsKey(2L));
            assertFalse(mem.getTargetAssociations(2).containsKey(4L));
        }
    }

    @Nested
    @DisplayName("tests for getSourceAssociations")
    class GetSourceAssociationsTests {

        @Test
        @DisplayName("test getSourceAssociations")
        void test1() {
            assertTrue(mem.getSourceAssociations(4).containsKey(2L));
            assertTrue(mem.getTargetAssociations(2).containsKey(4L));
        }
    }

    @Nested
    @DisplayName("tests for getTargetAssociations")
    class GetTargetAssociationsTests {

        @Test
        @DisplayName("test getTargetAssociations")
        void test1() {
            assertTrue(mem.getTargetAssociations(2).containsKey(4L));
        }
    }
}