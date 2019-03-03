# Getting Started

## Standalone Java Library
The REST API provides tools to create NGAC aware applications but requires a certain level of already developed infrastructure to really be useful.  If you are looking to just experiment with NGAC graphs and use cases, you can use the Policy Decision Point (PDP), Event Processing Point (EPP), and the Policy Access Point (PAP) packages to test different NGAC policy configurations in your own environments.

### Build Policy Machine JAR Library

1. In **pom.xml** change `<packaging>war</packaging>` to `<packaging>jar</packaging>`.
2. Run `mvn package` from the project root.
3. `pm.jar` will be created in /target/.  
4. Add `pm.jar` to your classpath.
Examples can be found below and in [examples](/examples/#standalone-examples)

### Example
We want to grant user `u1` read permission on object `o1`.  The following code sample will accomplish this.

```
// 1. Create a new graph.  For this example, we'll use the in-memory implementation of the 'Graph' interface.
Graph graph = new MemGraph();

// 2. Create the user and object nodes.
// create a user with name u1, ID 5, and the properties key1=value1
long u1ID = graph.createNode(new Node(5, "u1", NodeType.U, NodeUtils.toProperties("key1", "value1")));
// create an object with name o1, ID 3, and the properties key1=value1
long o1ID = graph.createNode(new Node(3, "o1", NodeType.O, NodeUtils.toProperties("key1", "value1")));

// 3. Create a user attribute 'ua1' and assign 'u1' to it.
long ua1ID = graph.createNode(new Node(4, "ua1", NodeType.UA, NodeUtils.toProperties("key1", "value1")));
graph.assign(new Node(u1ID, NodeType.U), new Node(ua1ID, NodeType.UA));

// 4. Create an object attribute 'oa1' and assign 'o1' to it.
long oa1ID = graph.createNode(new Node(2, "oa1", NodeType.OA, NodeUtils.toProperties("key1", "value1")));
graph.assign(new Node(o1ID, NodeType.O), new Node(oa1ID, NodeType.OA));

// 5. Create a policy class and assign the user and object attributes to it.
long pc1ID = graph.createNode(new Node(1, "pc1", NodeType.PC, NodeUtils.toProperties("key1", "value1")));
graph.assign(new Node(ua1ID, NodeType.UA), new Node(pc1ID, NodeType.PC));
graph.assign(new Node(oa1ID, NodeType.OA), new Node(pc1ID, NodeType.PC));

// 6. associate 'ua1' and 'oa1' to give 'u1' read permissions on 'o1'
graph.associate(new Node(ua1ID, NodeType.UA), new Node(oa1ID, NodeType.OA), new HashSet<>(Arrays.asList("read")));

// test the configuration is correct
// create a new policy decider with the in memory graph
// the second parameter is a list of prohibitions, but since they aren't used in this example, null is fine
Decider decider = new PReviewDecider(graph, null);

// use the listPermissions method to list the permissions 'u1' has on 'o1'
HashSet<String> permissions = decider.listPermissions(u1ID, 0, o1ID);
assertTrue(permissions.contains("read"));
```
