package gov.nist.csd.pm.pip.graph;

import gov.nist.csd.pm.model.exceptions.NoIDException;
import gov.nist.csd.pm.model.exceptions.NullNodeCtxException;
import gov.nist.csd.pm.model.exceptions.NullTypeException;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pip.loader.DummyLoader;
import gov.nist.csd.pm.pip.loader.LoaderException;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class NGACMemTest {

    NGACMem mem;

    @BeforeEach
    void setUp() {
        try {
            mem = new NGACMem(new DummyLoader());

            //create nodes
            Node pc1 = mem.createNode(new Node(1, "pc1", NodeType.PC, null));
            Node oa1 = mem.createNode(new Node(2, "oa1", NodeType.OA, null));
            Node o1 = mem.createNode(new Node(3, "o1", NodeType.O, null));
            Node ua1 = mem.createNode(new Node(4, "ua1", NodeType.UA, null));
            Node u1 = mem.createNode(new Node(5, "u1", NodeType.U, null));

            //create assignments
            mem.assign(oa1, pc1);
            mem.assign(o1, oa1);
            mem.assign(ua1, pc1);
            mem.assign(u1, ua1);

            //create associations
            mem.associate(ua1.getID(), oa1.getID(), new HashSet<>(Arrays.asList("read", "write")));
        }
        catch (LoaderException | NoIDException | NullNodeCtxException e) {
            fail(e.getMessage());
        }
    }

    @Nested
    @DisplayName("tests for createNode")
    class CreateNode {
        @Test
        @DisplayName("test create node")
        void test1() {
            try {
                Node test_oa = mem.createNode(new Node(6, "test_oa", NodeType.OA, null));
                assertTrue(mem.exists(test_oa.getID()));
            }
            catch (NoIDException | NullNodeCtxException e) {
                fail(e.getMessage());
            }
        }

        @Test
        @DisplayName("test create node NoIDException")
        void test2() {
            Assertions.assertThrows(NoIDException.class, () -> {
                mem.createNode(new Node("test_oa", NodeType.OA, null));
            });
        }

        @Test
        @DisplayName("test create node NullNodeCtxException")
        void test3() {
            Assertions.assertThrows(NullNodeCtxException.class, () -> {
                mem.createNode(null);
            });
        }
    }
}