# Getting Started

## Standalone Java Libraries
The REST API provides tools to create NGAC aware applications but requires a certain level of already developed infrastructure to really be useful.  If you are looking to just experiment with NGAC graphs and use cases, you can use the Policy Decision Point (PDP), Event Processing Point (EPP), and the Policy Access Point (PAP) packages to test different NGAC policy configurations.

### Example 1
This is an example of building a simple graph, with one of each type of node. We will use the `listPermissions` function to compute all the permissions `u1` has on `o1`.
```java
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
// this gives all users in ua1 read and write on all objects of oa1
graph.associate(ua1, oa1, OA, new HashSet<>(Arrays.asList("read", "write")));

// create a new policy decider which will be able to compute an access decision
Decider decider = new PReviewDecider(graph, Arrays.asList(prohibition));

// get a list of permissions that u1 has on o1
System.out.println(decider.listPermissions(u1, 0, o1));// print ["read", "write"]
```

## NGAC Basics

## Next Step
Utilize the administrative PEP to create PM aware applications
