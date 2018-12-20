package gov.nist.csd.pm.demos.standalone;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.prohibitions.Prohibition;
import gov.nist.csd.pm.common.model.prohibitions.ProhibitionNode;
import gov.nist.csd.pm.common.model.prohibitions.ProhibitionSubject;
import gov.nist.csd.pm.common.model.prohibitions.ProhibitionSubjectType;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.graph.MemGraph;
import gov.nist.csd.pm.pap.graph.Neo4jGraph;
import gov.nist.csd.pm.pap.loader.graph.DummyGraphLoader;
import gov.nist.csd.pm.pdp.engine.PReviewDecider;
import gov.nist.csd.pm.pdp.engine.Decider;

import java.util.Arrays;
import java.util.HashSet;

import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.*;

public class StandaloneExample {

    /**
     * Create a simple graph in memory
     */
    public static HashSet<String> runExample(Graph graph) {
        try {
            // create a node for each type
            long pc1 = graph.createNode(new Node(1, "pc1", PC, Node.toProperties("key1", "value1")));
            long oa1 = graph.createNode(new Node(2, "oa1", OA, Node.toProperties("key1", "value1")));
            long o1 = graph.createNode(new Node(3, "o1", O, Node.toProperties("key1", "value1")));
            long ua1 = graph.createNode(new Node(4, "ua1", UA, Node.toProperties("key1", "value1")));
            long u1 = graph.createNode(new Node(5, "u1", U, Node.toProperties("key1", "value1")));

            // create assignments
            graph.assign(o1, O, oa1, OA);
            graph.assign(oa1, OA, pc1, PC);
            graph.assign(u1, U, ua1, UA);
            graph.assign(ua1, UA, pc1, PC);

            // create an association
            graph.associate(ua1, oa1, OA, new HashSet<>(Arrays.asList("read", "write")));

            // create a prohibition for u1 on oa1
            Prohibition prohibition = new Prohibition();
            prohibition.setName("deny123");
            prohibition.setSubject(new ProhibitionSubject(u1, ProhibitionSubjectType.U));
            prohibition.setIntersection(false);
            prohibition.setOperations(new HashSet<>(Arrays.asList("read")));
            prohibition.addNode(new ProhibitionNode(oa1, false));

            // create a new policy decider
            Decider decider = new PReviewDecider(graph, Arrays.asList(prohibition));

            // get a list of permissions that u1 has on o1
            return decider.listPermissions(u1, 0, o1);
        }
        catch (PMException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            // run the example with an in memory graph
            Graph graph = new MemGraph(new DummyGraphLoader());

            HashSet<String> operations = runExample(graph);
            System.out.println(operations);

            // run the example with a neo4j graph
            graph = new Neo4jGraph(new DatabaseContext("localhost", 7687, "neo4j", "root", null));
            operations = runExample(graph);
            System.out.println(operations);
        }
        catch (PMException e) {
            e.printStackTrace();
        }
    }
}
