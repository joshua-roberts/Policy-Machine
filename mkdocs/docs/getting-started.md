# Getting Started

## Standalone Java Libraries
The REST API provides tools to create NGAC aware applications but requires a certain level of already developed infrastructure to really be useful.  If you are looking to just experiment with NGAC graphs and use cases, you can use the Policy Decision Point (PDP), Event Processing Point (EPP), and the Policy Access Point (PAP) packages to test different NGAC policy configurations.

### Build Policy Machine JAR Library

In order to package the project into a jar file, change `<packaging>war</packaging>` in pom.xml to `<packaging>jar</packaging>`.
Then run `mvn package` from the project root and pm.jar will be created in /target/. 

### Example 1
This is an example of building a simple graph, with one of each type of node. We will use the `listPermissions` function to compute all the permissions `u1` has on `o1`.
```java
// create a node for each type
long pc1 = graph.createNode(new NodeContext(1, "pc1", PC, NodeUtils.toProperties("key1", "value1")));
long oa1 = graph.createNode(new NodeContext(2, "oa1", OA, NodeUtils.toProperties("key1", "value1")));
long o1 = graph.createNode(new NodeContext(3, "o1", O, NodeUtils.toProperties("key1", "value1")));
long ua1 = graph.createNode(new NodeContext(4, "ua1", UA, NodeUtils.toProperties("key1", "value1")));
long u1 = graph.createNode(new NodeContext(5, "u1", U, NodeUtils.toProperties("key1", "value1")));

// create assignments
graph.assign(new NodeContext(o1, O), new NodeContext(oa1, OA));
graph.assign(new NodeContext(oa1, OA), new NodeContext(pc1, PC));
graph.assign(new NodeContext(u1, U), new NodeContext(ua1, UA));
graph.assign(new NodeContext(ua1, UA), new NodeContext(pc1, PC));

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
System.out.println(decider.listPermissions(u1, 0, o1));// print ["read", "write"]
```

## NGAC Basics

## Next Step
Utilize the administrative PEP to create PM aware applications
