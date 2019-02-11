# Getting Started

## Standalone Java Libraries
The REST API provides tools to create NGAC aware applications but requires a certain level of already developed infrastructure to really be useful.  If you are looking to just experiment with NGAC graphs and use cases, you can use the Policy Decision Point (PDP), Event Processing Point (EPP), and the Policy Access Point (PAP) packages to test different NGAC policy configurations in your own environments.

### Build Policy Machine JAR Library

1. In **pom.xml** change `<packaging>war</packaging>` to `<packaging>jar</packaging>`.
2. Run `mvn package` from the project root.
3. `pm.jar` will be created in /target/.  
4. Add `pm.jar` to your classpath.
Examples can be found below and in [demos](/demos/#standalone-examples)

### Example
This is an example of building a simple graph, with one of each type of node. We will use the `listPermissions` function to compute all the permissions `u1` has on `o1`.
```
// create a new in memory graph
Graph graph = new MemGraph();

// create a node for each type
long pc1ID = graph.createNode(new NodeContext(1, "pc1", PC, NodeUtils.toProperties("key1", "value1")));
long oa1ID = graph.createNode(new NodeContext(2, "oa1", OA, NodeUtils.toProperties("key1", "value1")));
long o1ID = graph.createNode(new NodeContext(3, "o1", O, NodeUtils.toProperties("key1", "value1")));
long ua1ID = graph.createNode(new NodeContext(4, "ua1", UA, NodeUtils.toProperties("key1", "value1")));
long u1ID = graph.createNode(new NodeContext(5, "u1", U, NodeUtils.toProperties("key1", "value1")));

// create assignments
graph.assign(new NodeContext(o1ID, O), new NodeContext(oa1ID, OA));
graph.assign(new NodeContext(oaID1, OA), new NodeContext(pc1ID, PC));
graph.assign(new NodeContext(u1ID, U), new NodeContext(ua1ID, UA));
graph.assign(new NodeContext(ua1ID, UA), new NodeContext(pc1ID, PC));

// create an association
graph.associate(new NodeContext(ua1ID, UA), new NodeContext(oa1ID, OA), new HashSet<>(Arrays.asList("read", "write")));

// create a prohibition for u1 on oa1
Prohibition prohibition = new Prohibition();
prohibition.setName("test_deny");
prohibition.setSubject(new ProhibitionSubject(u1, ProhibitionSubjectType.U));
prohibition.setIntersection(false);
prohibition.setOperations(new HashSet<>(Arrays.asList("read")));
prohibition.addNode(new ProhibitionNode(oa1, false));

// create a new policy decider with the in memory graph and a list of prohibitions
Decider decider = new PReviewDecider(graph, Arrays.asList(prohibition));

// print the list of permissions that u1 has on o1
System.out.println(decider.listPermissions(u1, NO_PROCESS, o1));// print ["read", "write"]
```
