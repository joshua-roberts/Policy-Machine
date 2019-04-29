package gov.nist.csd.pm.demos.demo;

import gov.nist.csd.pm.audit.Auditor;
import gov.nist.csd.pm.audit.PReviewAuditor;
import gov.nist.csd.pm.audit.model.Explain;
import gov.nist.csd.pm.audit.model.PolicyClass;
import gov.nist.csd.pm.decider.Decider;
import gov.nist.csd.pm.decider.PReviewDecider;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.Graph;
import gov.nist.csd.pm.graph.GraphSerializer;
import gov.nist.csd.pm.graph.MemGraph;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.graph.model.nodes.NodeType;
import gov.nist.csd.pm.pep.response.ApiResponse;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static gov.nist.csd.pm.graph.model.nodes.NodeType.*;

@Path("/demo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DemoResource {

    private static Graph graph;
    private static Script script;

    @Path("/scenarios")
    @GET
    public Response getScenarios() {
        String[] scenarios = new String[] {
                "test",
                "medrec-test"
        };

        return ApiResponse.Builder
                .success()
                .entity(scenarios)
                .build();
    }

    @Path("/scenarios/{name}")
    @GET
    public Response startScenario(@PathParam("name") String name) throws IOException, PMException {
        // get the scenario graph
        InputStream is = DemoResource.class.getClassLoader().getResourceAsStream("demos/demo/" + name + "/" + name + ".pm");
        if (is != null) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer, StandardCharsets.UTF_8);
            graph = new MemGraph();
            graph = GraphSerializer.fromJson(graph, writer.toString());
        } else {
            graph = new MemGraph();
        }

        is = DemoResource.class.getClassLoader().getResourceAsStream("demos/demo/" + name + "/" + name + ".yml");
        if(is != null) {
            script = ScriptParser.parse(is);
        } else {
            script = new Script();
        }

        return ApiResponse.Builder
                .success()
                .entity(script)
                .build();
    }

    @Path("/scenarios/{name}/steps/{step}")
    @POST
    public Response executeStep(@PathParam("name") String scenario,
                                @PathParam("step") int stepNum) throws PMException {
        if (script == null) {
            throw new PMException("initialize scenario at GET /scenarios/{name} first");
        }

        if (stepNum >= script.getSteps().size()) {
            throw new PMException("provided step does not exist in scenario " + scenario);
        }

        Step step = script.getSteps().get(stepNum);
        executeStep(step);

        return ApiResponse.Builder.success().build();
    }

    @Path("/users")
    @GET
    public Response getState() throws PMException {
        // username
        // BFA for each user
        // explain for each object in bfa object
        State state = new State();
        Decider decider = new PReviewDecider(graph);
        Auditor auditor = new PReviewAuditor(graph);

        Set<Node> users = graph.search(null, U.toString(), null);
        for(Node user : users) {
            Audit audit = new Audit();

            Map<Long, Set<String>> accessibleNodes = decider.getAccessibleNodes(user.getID());
            for(long objectID : accessibleNodes.keySet()) {
                Node obj = graph.getNode(objectID);
                if(!obj.getType().equals(O)) {
                    continue;
                }

                Explain explain = auditor.explain(user.getID(), objectID);

                List<AuditPolicyClass> pcs = new ArrayList<>();
                for(String pcName : explain.getPolicyClasses().keySet()) {
                    PolicyClass policyClass = explain.getPolicyClasses().get(pcName);
                    pcs.add(new AuditPolicyClass(pcName, policyClass.getOperations(), policyClass.getPaths()));
                }

                audit.addObject(new AuditObject(obj.getName(), explain.getOperations(), pcs));
            }

            state.addUser(new UserState(user.getName(), audit));
        }

        return ApiResponse.Builder.success().entity(state).build();
    }

    private static class State {
        private List<UserState> users;

        public State() {
            users = new ArrayList<>();
        }

        public List<UserState> getUsers() {
            return users;
        }

        public void setUsers(List<UserState> users) {
            this.users = users;
        }

        public void addUser(UserState user) {
            this.users.add(user);
        }
    }

    private static class UserState {
        private String user;
        private Audit audit;

        public UserState() {}

        public UserState(String user, Audit audit) {
            this.user = user;
            this.audit = audit;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public Audit getAudit() {
            return audit;
        }

        public void setAudit(Audit audit) {
            this.audit = audit;
        }
    }

    private static class Audit {
        private List<AuditObject> objects;

        public Audit() {
            objects = new ArrayList<>();
        }

        public List<AuditObject> getObjects() {
            return objects;
        }

        public void setObjects(List<AuditObject> objects) {
            this.objects = objects;
        }

        public void addObject(AuditObject object) {
            this.objects.add(object);
        }
    }

    private static class AuditObject {
        private String object;
        private Set<String> permissions;
        private List<AuditPolicyClass> explain;

        public AuditObject() {
            permissions = new HashSet<>();
            explain = new ArrayList<>();
        }

        public AuditObject(String object, Set<String> permissions, List<AuditPolicyClass> explain) {
            this.object = object;
            this.permissions = permissions;
            this.explain = explain;
        }

        public String getObject() {
            return object;
        }

        public void setObject(String object) {
            this.object = object;
        }

        public Set<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(Set<String> permissions) {
            this.permissions = permissions;
        }

        public List<AuditPolicyClass> getExplain() {
            return explain;
        }

        public void setExplain(List<AuditPolicyClass> explain) {
            this.explain = explain;
        }
    }

    private static class AuditPolicyClass {
        private String policyClass;
        private Set<String> operations;
        private List<gov.nist.csd.pm.audit.model.Path> paths;

        public AuditPolicyClass() {
            paths = new ArrayList<>();
        }

        public AuditPolicyClass(String policyClass, Set<String> operations, List<gov.nist.csd.pm.audit.model.Path> paths) {
            this.policyClass = policyClass;
            this.operations = operations;
            this.paths = paths;
        }

        public String getPolicyClass() {
            return policyClass;
        }

        public void setPolicyClass(String policyClass) {
            this.policyClass = policyClass;
        }

        public Set<String> getOperations() {
            return operations;
        }

        public void setOperations(Set<String> operations) {
            this.operations = operations;
        }

        public List<gov.nist.csd.pm.audit.model.Path> getPaths() {
            return paths;
        }

        public void setPaths(List<gov.nist.csd.pm.audit.model.Path> paths) {
            this.paths = paths;
        }

        public void addPath(gov.nist.csd.pm.audit.model.Path path) {
            this.paths.add(path);
        }
    }

    private void executeStep(Step step) throws PMException {
        List<String> actions = step.getActions();
        for (String action : actions) {
            String[] tokens = action.split(" ");
            switch (tokens[0]) {
                case "assign":
                    assign(tokens);
                    break;
                case "associate":
                    association(tokens);
                    break;
                case "node":
                    node(tokens);
                    break;
                case "delete":
                    delete(tokens);
                    break;
                case "deassign":
                    deassign(tokens);
                    break;
                case "dissociate":
                    dissociate(tokens);
                    break;
                default:
                    throw new PMException("invalid command");
            }
        }
    }

    private void node(String[] tokens) throws PMException {
        if (tokens.length != 3) {
            throw new PMException("invalid node command");
        }

        String type = tokens[1];
        String name = tokens[2];

        graph.createNode(new Random().nextLong(), name, NodeType.toNodeType(type), null);
    }

    private void delete(String[] tokens) throws PMException {
        if (tokens.length != 2) {
            throw new PMException("invalid delete command");
        }

        String name = tokens[1];
        Set<Node> nodes = graph.search(name, null, null);
        if (nodes.isEmpty()) {
            throw new PMException("no node with name " + name);
        }
        Node node = nodes.iterator().next();

        graph.deleteNode(node.getID());
    }

    private void association(String[] tokens) throws PMException {
        if (tokens.length != 4) {
            throw new PMException("invalid association command");
        }

        String source = tokens[1];
        String target = tokens[2];

        Set<Node> nodes = graph.search(source, null, null);
        if (nodes.isEmpty()) {
            throw new PMException("no source node with name " + source);
        }
        Node sourceNode = nodes.iterator().next();

        nodes = graph.search(target, null, null);
        if(nodes.isEmpty()) {
            throw new PMException("no target node with name " + target);
        }
        Node targetNode = nodes.iterator().next();

        graph.associate(sourceNode.getID(), targetNode.getID(), new HashSet<>(Arrays.asList(tokens[3].split(","))));
    }

    private void dissociate(String[] tokens) throws PMException {
        if (tokens.length != 4) {
            throw new PMException("invalid dissociate command");
        }

        String source = tokens[1];
        String target = tokens[2];

        Set<Node> nodes = graph.search(source, null, null);
        if (nodes.isEmpty()) {
            throw new PMException("no source node with name " + source);
        }
        Node sourceNode = nodes.iterator().next();

        nodes = graph.search(target, null, null);
        if (nodes.isEmpty()) {
            throw new PMException("no target node with name " + target);
        }
        Node targetNode = nodes.iterator().next();

        graph.dissociate(sourceNode.getID(), targetNode.getID());
    }

    private void assign(String[] tokens) throws PMException {
        if (tokens.length != 3) {
            throw new PMException("invalid assign command");
        }

        String child = tokens[1];
        String parent = tokens[2];

        Set<Node> nodes = graph.search(child, null, null);
        if (nodes.isEmpty()) {
            throw new PMException("no child node with name " + child);
        }
        Node childNode = nodes.iterator().next();

        nodes = graph.search(parent, null, null);
        if(nodes.isEmpty()) {
            throw new PMException("no parent node with name " + parent);
        }
        Node parentNode = nodes.iterator().next();

        graph.assign(childNode.getID(), parentNode.getID());
    }

    private void deassign(String[] tokens) throws PMException {
        if (tokens.length != 3) {
            throw new PMException("invalid deassign command");
        }

        String child = tokens[1];
        String parent = tokens[2];

        Set<Node> nodes = graph.search(child, null, null);
        if (nodes.isEmpty()) {
            throw new PMException("no child node with name " + child);
        }
        Node childNode = nodes.iterator().next();

        nodes = graph.search(parent, null, null);
        if(nodes.isEmpty()) {
            throw new PMException("no parent node with name " + parent);
        }
        Node parentNode = nodes.iterator().next();

        graph.deassign(childNode.getID(), parentNode.getID());
    }

    public static void main(String[] args) throws PMException, IOException {
        Random rand = new Random();

        Graph graph = new MemGraph();

// create nodes
// object attributes
        Node salariesNode = graph.createNode(rand.nextLong(), "Salaries", OA, null);
        Node ssnsNode = graph.createNode(rand.nextLong(), "SSNs", OA, null);
        Node grp1SalariesNode = graph.createNode(rand.nextLong(), "Grp1 Salaries", OA, null);
        Node grp2SalariesNode = graph.createNode(rand.nextLong(), "Grp2 Salaries", OA, null);
        Node publicNode = graph.createNode(rand.nextLong(), "Public Info", OA, null);

        Node bobRecNode = graph.createNode(rand.nextLong(), "Bob Record", OA, null);
        Node bobRNode = graph.createNode(rand.nextLong(), "Bob r", OA, null);
        Node bobRWNode = graph.createNode(rand.nextLong(), "Bob r/w", OA, null);

        Node aliceRecNode = graph.createNode(rand.nextLong(), "Alice Record", OA, null);
        Node aliceRNode = graph.createNode(rand.nextLong(), "Alice r", OA, null);
        Node aliceRWNode = graph.createNode(rand.nextLong(), "Alice r/w", OA, null);

// objects for bob's name, salary, and ssn
        Node bobNameNode = graph.createNode(rand.nextLong(), "bob name", O, null);
        Node bobSalaryNode = graph.createNode(rand.nextLong(), "bob salary", O, null);
        Node bobSSNNode = graph.createNode(rand.nextLong(), "bob ssn", O, null);

// objects for alice's name, salary, and ssn
        Node aliceNameNode = graph.createNode(rand.nextLong(), "alice name", O, null);
        Node aliceSalaryNode = graph.createNode(rand.nextLong(), "alice salary", O, null);
        Node aliceSSNNode = graph.createNode(rand.nextLong(), "alice ssn", O, null);

// user attributes
        Node hrNode = graph.createNode(rand.nextLong(), "HR", UA, null);
        Node grp1MgrNode = graph.createNode(rand.nextLong(), "Grp1Mgr", UA, null);
        Node grp2MgrNode = graph.createNode(rand.nextLong(), "Grp2Mgr", UA, null);
        Node staffNode = graph.createNode(rand.nextLong(), "Staff", UA, null);
        Node bobUANode = graph.createNode(rand.nextLong(), "Bob", UA, null);
        Node aliceUANode = graph.createNode(rand.nextLong(), "Alice", UA, null);

// users
        Node bobNode = graph.createNode(rand.nextLong(), "bob", U, null);
        Node aliceNode = graph.createNode(rand.nextLong(), "alice", U, null);
        Node charlieNode = graph.createNode(rand.nextLong(), "charlie", U, null);

// policy class
        Node pcNode = graph.createNode(rand.nextLong(), "Employee Records", PC, null);


// assignments
// assign users to user attributes
        graph.assign(charlieNode.getID(), hrNode.getID());
        graph.assign(bobNode.getID(), grp1MgrNode.getID());
        graph.assign(aliceNode.getID(), grp2MgrNode.getID());
        graph.assign(charlieNode.getID(), staffNode.getID());
        graph.assign(bobNode.getID(), staffNode.getID());
        graph.assign(aliceNode.getID(), staffNode.getID());
        graph.assign(bobNode.getID(), bobUANode.getID());
        graph.assign(aliceNode.getID(), aliceUANode.getID());

// assign objects to object attributes
// salary objects
        graph.assign(bobSalaryNode.getID(), salariesNode.getID());
        graph.assign(bobSalaryNode.getID(), grp1SalariesNode.getID());
        graph.assign(bobSalaryNode.getID(), bobRNode.getID());

        graph.assign(aliceSalaryNode.getID(), salariesNode.getID());
        graph.assign(aliceSalaryNode.getID(), grp2SalariesNode.getID());
        graph.assign(aliceSalaryNode.getID(), aliceRNode.getID());

// ssn objects
        graph.assign(bobSSNNode.getID(), ssnsNode.getID());
        graph.assign(bobSSNNode.getID(), bobRWNode.getID());

        graph.assign(aliceSSNNode.getID(), ssnsNode.getID());
        graph.assign(aliceSSNNode.getID(), aliceRWNode.getID());

// name objects
        graph.assign(bobNameNode.getID(), publicNode.getID());
        graph.assign(bobNameNode.getID(), bobRWNode.getID());

        graph.assign(aliceNameNode.getID(), publicNode.getID());
        graph.assign(aliceNameNode.getID(), aliceRWNode.getID());

// bob and alice r/w containers to their records
        graph.assign(bobRNode.getID(), bobRecNode.getID());
        graph.assign(bobRWNode.getID(), bobRecNode.getID());

        graph.assign(aliceRNode.getID(), aliceRecNode.getID());
        graph.assign(aliceRWNode.getID(), aliceRecNode.getID());


// assign object attributes to policy classes
        graph.assign(salariesNode.getID(), pcNode.getID());
        graph.assign(ssnsNode.getID(), pcNode.getID());
        graph.assign(grp1SalariesNode.getID(), pcNode.getID());
        graph.assign(grp2SalariesNode.getID(), pcNode.getID());
        graph.assign(publicNode.getID(), pcNode.getID());
        graph.assign(bobRecNode.getID(), pcNode.getID());
        graph.assign(aliceRecNode.getID(), pcNode.getID());

// associations
        Set<String> rw = new HashSet<>(Arrays.asList("r", "w"));
        Set<String> r = new HashSet<>(Arrays.asList("r"));

        graph.associate(hrNode.getID(), salariesNode.getID(), rw);
        graph.associate(hrNode.getID(), ssnsNode.getID(), rw);
        graph.associate(grp1MgrNode.getID(), grp1SalariesNode.getID(), r);
        graph.associate(grp2MgrNode.getID(), grp2SalariesNode.getID(), r);
        graph.associate(staffNode.getID(), publicNode.getID(), r);
        graph.associate(bobUANode.getID(), bobRWNode.getID(), rw);
        graph.associate(bobUANode.getID(), bobRNode.getID(), r);
        graph.associate(aliceUANode.getID(), aliceRWNode.getID(), rw);
        graph.associate(aliceUANode.getID(), aliceRNode.getID(), r);

        System.out.println(GraphSerializer.toJson(graph));
    }
}
