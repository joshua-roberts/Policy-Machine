package gov.nist.csd.pm.demos.ndac.pep;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.common.model.prohibitions.Prohibition;
import gov.nist.csd.pm.demos.ndac.algorithms.parsing.v1.Algorithm;
import gov.nist.csd.pm.demos.ndac.algorithms.parsing.v1.SelectAlgorithm;
import gov.nist.csd.pm.demos.ndac.algorithms.parsing.v2.AlgorithmV2;
import gov.nist.csd.pm.demos.ndac.algorithms.parsing.v2.SelectAlgorithmV2;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.db.sql.SQLConnection;
import gov.nist.csd.pm.pap.graph.MemGraph;
import gov.nist.csd.pm.pap.loader.graph.DummyGraphLoader;
import gov.nist.csd.pm.pap.search.MemGraphSearch;
import gov.nist.csd.pm.pep.response.ApiResponse;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import static gov.nist.csd.pm.common.constants.Properties.NAMESPACE_PROPERTY;
import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.*;

@Path("/demos/ndac")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NDACResource {

    public static void main(String[] args) throws PMException, JSQLParserException, ClassNotFoundException, SQLException, IOException {
        MemGraph graph = new MemGraph(new DummyGraphLoader());
        //Graph graph = new Neo4jGraph(new DatabaseContext("localhost", 7687, "neo4j", "root", null));
        NGACContext ngacCtx = buildGraph(graph, 0);
        String sql = "select employee_info.name, employee_info.ssn, employee_info.salary from employee_info limit 4";
        Select select = (Select) CCJSqlParserUtil.parse(sql);

        MemGraphSearch search = new MemGraphSearch(graph);
        HashSet<Node> nodes = search.search("dave", NodeType.U.toString(), null);
        Node bob = nodes.iterator().next();
        System.out.println("alice ID: " + bob.getID());

        DatabaseContext ctx = new DatabaseContext("localhost", 3306, "root", "root", "employees");
        AlgorithmV2 algorithm = new SelectAlgorithmV2(select, bob.getID(), 0, ctx, ngacCtx.getGraph(), search, ngacCtx.getProhibitions());

        long start = System.nanoTime();
        String result = algorithm.run();
        long time = System.nanoTime() - start;


        System.out.println("original sql: " + sql);
        System.out.println("permitted sql: " + result);
        System.out.println("time: " + (double)time/1000000000);
        /*for (int i = 1; i <= 1000; i++) {
            System.out.println("insert into employee_info (id, name, ssn, salary) values (" + (i+4) + ", 'emp_" + i + "', " + ThreadLocalRandom
                    .current().nextInt(100000000, 999999999) + ", " + ThreadLocalRandom.current().nextInt(30000, 100000 + 1) + ");");
        }*/
    }

    private static final String EMPLOYEES = "employees";
    private static final String EMPLOYEE_INFO = "employee_info";
    private static final String EMPLOYEE_CONTACT = "employee_contact";

    @POST
    public Response run(NDACRequest request) throws PMException, JSQLParserException, ClassNotFoundException, SQLException, IOException {
        MemGraph graph = new MemGraph(new DummyGraphLoader());
        NGACContext ctx = buildGraph(graph, 1000);
        NDACResponse response = new NDACResponse();
        switch(request.getAlgorithm()) {
            case "parsing-v1.0":
                response = runParsing("parsing-v1.0", request, ctx.getGraph(), ctx.getProhibitions());
                break;
            case "parsing-v2.0":
                response = runParsing("parsing-v2.0", request, ctx.getGraph(), ctx.getProhibitions());
                break;
        }

        return ApiResponse.Builder
                .success()
                .entity(response)
                .build();
    }

    private NDACResponse runParsing(String version, NDACRequest request, MemGraph graph, List<Prohibition> prohibitions) throws JSQLParserException, PMException, ClassNotFoundException, SQLException, IOException {
        NDACResponse response = new NDACResponse();
        MemGraphSearch search = new MemGraphSearch((MemGraph) graph);
        HashSet<Node> nodes = search.search(null, NodeType.U.toString(), null);
        for(Node user : nodes) {
            String originalSQL = request.getSql();
            Select select = (Select) CCJSqlParserUtil.parse(originalSQL);

            System.out.println("user: " + user.getName());
            NDACResponse.NDACResult userResult = new NDACResponse.NDACResult();
            userResult.setUser(user.getName());

            // start timing the algorithm
            long start = System.nanoTime();
            double time;

            DatabaseContext ctx = new DatabaseContext(request.getHost(), request.getPort(), request.getUsername(), request.getPassword(), request.getDatabase());
            String permittedSQL = "";
            switch(version) {
                case "parsing-v1.0":
                    Algorithm algorithm = new SelectAlgorithm(select, user.getID(), 0, ctx, graph, search, prohibitions);
                    // translate the original sql into permitted sql
                    permittedSQL = algorithm.run();
                    break;
                case "parsing-v2.0":
                    AlgorithmV2 algorithmV2 = new SelectAlgorithmV2(select, user.getID(), 0, ctx, graph, search, prohibitions);
                    permittedSQL = algorithmV2.run();
                    break;
            }

            // get the userResult of the permitted sql from the database
            SQLConnection connection = new SQLConnection("localhost", 3306, "root", "root", "employees");
            Connection conn = connection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(permittedSQL)) {
                time = (double)(System.nanoTime() - start) / 1000000000;

                List<HashMap<String, String>> results = new ArrayList<>();
                HashSet<String> columns = new HashSet<>();
                ResultSetMetaData metaData = rs.getMetaData();
                while (rs.next()) {
                    HashMap<String, String> map = new HashMap<>();
                    int columnCount = metaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        if (columnName.contains(".")) {
                            columnName = columnName.split("\\.")[1];
                        }
                        columns.add(columnName);
                        Object object = rs.getObject(i);
                        String value = null;
                        if (object != null) {
                            value = String.valueOf(object);
                        }
                        map.put(columnName, value);
                    }
                    results.add(map);
                }
                userResult.setData(new NDACResponse.SQLResults(columns, results));
            }

            HashSet<Node> parents = graph.getParents(user.getID());
            List<String> attrs = new ArrayList<>();
            for(Node node : parents) {
                attrs.add(node.getName());
            }

            userResult.setAttributes(attrs);
            userResult.setPermittedSQL(permittedSQL);
            userResult.setTime(time);

            response.addResult(userResult);

            System.out.println("original sql: " + originalSQL);
            System.out.println("permitted sql: " + permittedSQL);
            System.out.println("time: " + time);
        }

        return response;
    }

    /**
     * build a graph that represents a database
     * 2 tables: employee_info(name, ssn, salary) employee_phone(phone)
     *
     * 6 records
     * 2 groups
     * users: bob - grp1mgr, alice - grp2mgr, charlie - HR, dave, lucy, chris
     *
     * @param numEmployees the number of records to add on top of the aforementioned users
     * @return the NGAC graph with a simple policy configuration of this "database"
     */
    public static NGACContext buildGraph(MemGraph graph, int numEmployees) throws PMException {
        //create employees pc
        long pcID = graph.createNode(new Node(getID(), EMPLOYEES, PC, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEES)));
        long baseID = graph.createNode(new Node(getID(), EMPLOYEES, OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEES)));
        graph.assign(baseID, OA, pcID, PC);

        //create employee_info table
        long empInfoTableID = graph.createNode(new Node(getID(), EMPLOYEE_INFO, OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEES)));
        graph.assign(empInfoTableID, OA, baseID, OA);

        long rowsID = graph.createNode(new Node(getID(), "rows", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        long colsID = graph.createNode(new Node(getID(), "columns", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(rowsID, OA, empInfoTableID, OA);
        graph.assign(colsID, OA, empInfoTableID, OA);

        //create column nodes
        long idID = graph.createNode(new Node(getID(), "id", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO, "pk", "true", "schema_comp", "column")));
        graph.assign(idID, OA, colsID, OA);
        long nameID = graph.createNode(new Node(getID(), "name", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO, "schema_comp", "column")));
        graph.assign(nameID, OA, colsID, OA);
        long ssnID = graph.createNode(new Node(getID(), "ssn", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO, "schema_comp", "column")));
        graph.assign(ssnID, OA, colsID, OA);
        long salaryID = graph.createNode(new Node(getID(), "salary", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO, "schema_comp", "column")));
        graph.assign(salaryID, OA, colsID, OA);


        List<Long> group1Records = new ArrayList<>();
        List<Long> group2Records = new ArrayList<>();

        //bob row
        long bobInfoID = graph.createNode(new Node(getID(), "1", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(bobInfoID, OA, rowsID, OA);
        group1Records.add(bobInfoID);
        long oID = graph.createNode(new Node(getID(), "bob_id", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, bobInfoID, OA);
        graph.assign(oID, O, idID, OA);
        oID = graph.createNode(new Node(getID(), "bob_name", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, bobInfoID, OA);
        graph.assign(oID, O, nameID, OA);
        oID = graph.createNode(new Node(getID(), "bob_ssn", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, bobInfoID, OA);
        graph.assign(oID, O, ssnID, OA);
        oID = graph.createNode(new Node(getID(), "bob_salary", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, bobInfoID, OA);
        graph.assign(oID, O, salaryID, OA);

        //alice row
        long aliceInfoID = graph.createNode(new Node(getID(), "2", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(aliceInfoID, OA, rowsID, OA);
        group2Records.add(aliceInfoID);
        oID = graph.createNode(new Node(getID(), "alice_id", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, aliceInfoID, OA);
        graph.assign(oID, O, idID, OA);
        oID = graph.createNode(new Node(getID(), "alice_name", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, aliceInfoID, OA);
        graph.assign(oID, O, nameID, OA);
        oID = graph.createNode(new Node(getID(), "alice_ssn", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, aliceInfoID, OA);
        graph.assign(oID, O, ssnID, OA);
        oID = graph.createNode(new Node(getID(), "alice_salary", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, aliceInfoID, OA);
        graph.assign(oID, O, salaryID, OA);

        //charlie row
        long charlieInfoID = graph.createNode(new Node(getID(), "3", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(charlieInfoID, OA, rowsID, OA);
        oID = graph.createNode(new Node(getID(), "charlie_id", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, charlieInfoID, OA);
        graph.assign(oID, O, idID, OA);
        oID = graph.createNode(new Node(getID(), "charlie_name", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, charlieInfoID, OA);
        graph.assign(oID, O, nameID, OA);
        oID = graph.createNode(new Node(getID(), "charlie_ssn", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, charlieInfoID, OA);
        graph.assign(oID, O, ssnID, OA);
        oID = graph.createNode(new Node(getID(), "charlie_salary", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, charlieInfoID, OA);
        graph.assign(oID, O, salaryID, OA);

        //dave row
        long daveInfoID = graph.createNode(new Node(getID(), "4", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(daveInfoID, OA, rowsID, OA);
        group1Records.add(daveInfoID);
        oID = graph.createNode(new Node(getID(), "dave_id", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, daveInfoID, OA);
        graph.assign(oID, O, idID, OA);
        oID = graph.createNode(new Node(getID(), "dave_name", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, daveInfoID, OA);
        graph.assign(oID, O, nameID, OA);
        oID = graph.createNode(new Node(getID(), "dave_ssn", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, daveInfoID, OA);
        graph.assign(oID, O, ssnID, OA);
        oID = graph.createNode(new Node(getID(), "dave_salary", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, daveInfoID, OA);
        graph.assign(oID, O, salaryID, OA);

        for (int i = 1; i <= numEmployees; i++) {
            System.out.println(String.valueOf(i + 4));
            long empID = graph.createNode(new Node(getID(), String.valueOf(i + 4), OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
            graph.assign(empID, OA, rowsID, OA);
            oID = graph.createNode(new Node(getID(), String.format("emp_%d_id", empID), O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
            graph.assign(oID, O, empID, OA);
            graph.assign(oID, O, idID, OA);
            oID = graph.createNode(new Node(getID(), String.format("emp_%d_name", empID), O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
            graph.assign(oID, O, empID, OA);
            graph.assign(oID, O, nameID, OA);
            oID = graph.createNode(new Node(getID(), String.format("emp_%d_ssn", empID), O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
            graph.assign(oID, O, empID, OA);
            graph.assign(oID, O, ssnID, OA);
            oID = graph.createNode(new Node(getID(), String.format("emp_%d_salary", empID), O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
            graph.assign(oID, O, empID, OA);
            graph.assign(oID, O, salaryID, OA);

            if(i%2 == 0) {
                group1Records.add(empID);
            } else {
                group2Records.add(empID);
            }
        }

        System.out.println("creating users");
        //create users
        long bobID = graph.createNode(new Node(getID(), "bob", U, null));
        long aliceID = graph.createNode(new Node(getID(), "alice", U, null));
        long daveID = graph.createNode(new Node(getID(), "dave", U, null));
        long charlieID = graph.createNode(new Node(getID(), "charlie", U, null));

        long usersID = graph.createNode(new Node(getID(), "users", UA, null));
        graph.assign(bobID, U, usersID, UA);
        graph.assign(aliceID, U, usersID, UA);
        graph.assign(daveID, U, usersID, UA);
        graph.assign(charlieID, U, usersID, UA);

        graph.assign(usersID, UA, pcID, PC);

        graph.associate(usersID, baseID, OA, new HashSet<>(Arrays.asList("read")));

        System.out.println("configuring RBAC");
        System.out.println(group1Records);
        System.out.println(group2Records);

        //rbac
        pcID = graph.createNode(new Node(getID(), "RBAC", PC, null));
        //records
        long empRecID = graph.createNode(new Node(getID(), "records", OA, null));
        graph.assign(empRecID, OA, pcID, PC);
        //group 1 records
        long group1ID = graph.createNode(new Node(getID(), "group 1 records", OA, null));
        graph.assign(group1ID, OA, empRecID, OA);
        for(long recID : group1Records) {
            graph.assign(recID, OA, group1ID, OA);
        }
        //group 2 records
        long group2ID = graph.createNode(new Node(getID(), "group 2 records", OA, null));
        graph.assign(group2ID, OA, empRecID, OA);
        for(long recID : group2Records) {
            graph.assign(recID, OA, group2ID, OA);
        }
        long hrID = graph.createNode(new Node(getID(), "hr records", OA, null));
        graph.assign(hrID, OA, empRecID, OA);
        graph.assign(charlieInfoID, OA, hrID, OA);

        // group uas
        long empsID = graph.createNode(new Node(getID(), "employees", UA, null));
        graph.assign(empsID, UA, pcID, PC);
        long grp1UA = graph.createNode(new Node(getID(), "grp1", UA, null));
        long grp2UA = graph.createNode(new Node(getID(), "grp2", UA, null));
        long hrUAID = graph.createNode(new Node(getID(), "hr", UA, null));
        graph.assign(hrUAID, UA, empsID, UA);
        graph.assign(grp1UA, UA, empsID, UA);
        graph.assign(grp2UA, UA, empsID, UA);

        graph.assign(bobID, U, grp1UA, UA);
        graph.assign(aliceID, U, grp2UA, UA);
        graph.assign(charlieID, U, hrUAID, UA);
        graph.assign(daveID, U, grp1UA, UA);

        graph.associate(grp1UA, group1ID, OA, new HashSet<>(Arrays.asList("read")));
        graph.associate(grp2UA, group2ID, OA, new HashSet<>(Arrays.asList("read")));
        graph.associate(hrUAID, empRecID, OA, new HashSet<>(Arrays.asList("read")));

        //DAC
        pcID = graph.createNode(new Node(getID(), "DAC", PC, null));
        //homes
        long bobHomeID = graph.createNode(new Node(getID(), "bob home", OA, null));
        long aliceHomeID = graph.createNode(new Node(getID(), "alice home", OA, null));
        long daveHomeID = graph.createNode(new Node(getID(), "dave home", OA, null));
        long charlieHomeID = graph.createNode(new Node(getID(), "charlie home", OA, null));
        graph.assign(bobHomeID, OA, pcID, PC);
        graph.assign(aliceHomeID, OA, pcID, PC);
        graph.assign(daveHomeID, OA, pcID, PC);
        graph.assign(charlieHomeID, OA, pcID, PC);

        graph.assign(group1ID, OA, bobHomeID, OA);
        graph.assign(group2ID, OA, aliceHomeID, OA);
        graph.assign(empRecID, OA, charlieHomeID, OA);
        graph.assign(daveInfoID, OA, daveHomeID, OA);

        long uaID = graph.createNode(new Node(getID(), "Bob", UA, null));
        graph.assign(bobID, U, uaID, UA);
        graph.associate(uaID, bobHomeID, OA, new HashSet<>(Arrays.asList("read")));
        graph.assign(group1ID, OA, bobHomeID, OA);

        uaID = graph.createNode(new Node(getID(), "Alice", UA, null));
        graph.assign(aliceID, U, uaID, UA);
        graph.associate(uaID, aliceHomeID, OA, new HashSet<>(Arrays.asList("read")));
        graph.assign(group2ID, OA, aliceHomeID, OA);

        uaID = graph.createNode(new Node(getID(), "Dave", UA, null));
        graph.assign(daveID, U, uaID, UA);
        graph.associate(uaID, daveHomeID, OA, new HashSet<>(Arrays.asList("read")));
        graph.assign(daveInfoID, OA, daveHomeID, OA);

        uaID = graph.createNode(new Node(getID(), "Charlie", UA, null));
        graph.assign(charlieID, U, uaID, UA);
        graph.associate(uaID, charlieHomeID, OA, new HashSet<>(Arrays.asList("read")));
        graph.assign(empRecID, OA, charlieHomeID, OA);

        List<Prohibition> prohibitions = new ArrayList<>();
        //uncomment the below code to add prohibitions
        //prohibitions
        /*
        //bob - ssn
        Prohibition prohibition = new Prohibition();
        prohibition.setName("bob_deny_ssn");
        prohibition.setSubject(new ProhibitionSubject(bobID, ProhibitionSubjectType.U));
        prohibition.setIntersection(true);
        prohibition.setOperations(new HashSet<>(Arrays.asList("read")));
        prohibition.addNode(new ProhibitionNode(ssnID, false));
        prohibition.addNode(new ProhibitionNode(bobInfoID, true));
        prohibitions.add(prohibition);
        //alice ssn
        prohibition = new Prohibition();
        prohibition.setName("alice_deny_ssn");
        prohibition.setSubject(new ProhibitionSubject(aliceID, ProhibitionSubjectType.U));
        prohibition.setIntersection(true);
        prohibition.setOperations(new HashSet<>(Arrays.asList("read")));
        prohibition.addNode(new ProhibitionNode(ssnID, false));
        prohibition.addNode(new ProhibitionNode(aliceInfoID, true));
        prohibitions.add(prohibition);

        // dave ssn and salary
        prohibition = new Prohibition();
        prohibition.setName("dave_deny_ssn");
        prohibition.setSubject(new ProhibitionSubject(daveID, ProhibitionSubjectType.U));
        prohibition.setIntersection(true);
        prohibition.setOperations(new HashSet<>(Arrays.asList("read")));
        prohibition.addNode(new ProhibitionNode(ssnID, false));
        prohibition.addNode(new ProhibitionNode(daveInfoID, true));
        prohibitions.add(prohibition);
        prohibition = new Prohibition();
        prohibition.setName("dave_deny_salary");
        prohibition.setSubject(new ProhibitionSubject(daveID, ProhibitionSubjectType.U));
        prohibition.setIntersection(true);
        prohibition.setOperations(new HashSet<>(Arrays.asList("read")));
        prohibition.addNode(new ProhibitionNode(salaryID, false));
        prohibition.addNode(new ProhibitionNode(daveInfoID, true));
        prohibitions.add(prohibition);*/

        System.out.println("done!");

        return new NGACContext(graph, prohibitions);
    }

    private static long getID() {
        return new Random().nextLong();
    }

    static class NGACContext {
        MemGraph graph;
        List<Prohibition> prohibitions;

        public NGACContext(MemGraph graph, List<Prohibition> prohibitions) {
            this.graph = graph;
            this.prohibitions = prohibitions;
        }

        public MemGraph getGraph() {
            return graph;
        }

        public List<Prohibition> getProhibitions() {
            return prohibitions;
        }
    }
}
