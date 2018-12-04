package gov.nist.csd.pm.demos.ndac.pep;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.demos.ndac.algorithms.v1.Algorithm;
import gov.nist.csd.pm.demos.ndac.algorithms.v1.SelectAlgorithm;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.db.sql.SQLConnection;
import gov.nist.csd.pm.pap.graph.MemGraphExt;
import gov.nist.csd.pm.pap.loader.graph.DummyGraphLoader;
import gov.nist.csd.pm.pap.search.MemGraphExtSearch;
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

    public static void main(String[] args) throws PMException, JSQLParserException, ClassNotFoundException, SQLException, InvalidEntityException, IOException {
        Graph graph = new MemGraphExt(new DummyGraphLoader());
        graph = buildGraph(graph, 1000);
        String sql = "select employee_info.name, employee_info.ssn, employee_info.salary from employee_info";
        Select select = (Select) CCJSqlParserUtil.parse(sql);

        MemGraphExtSearch search = new MemGraphExtSearch((MemGraphExt) graph);
        HashSet<Node> nodes = search.search("bob", NodeType.U.toString(), null);
        Node bob = nodes.iterator().next();
        System.out.println("bob ID: " + bob.getID());

        DatabaseContext ctx = new DatabaseContext("localhost", 3306, "root", "root", "employees");
        Algorithm algorithm = new SelectAlgorithm(select, bob.getID(), 0, ctx, graph, search, null);

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

    private List<Approach> approaches = new ArrayList<>();
    {
        approaches.add(new Approach("parsing-v1.0", "description on parsing v1"));
    }

    class Approach {
        String name;
        String description;

        public Approach() {

        }

        public Approach(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    @GET
    public Response getApproaches() {
        return ApiResponse.Builder
                .success()
                .entity(approaches)
                .build();
    }

    @POST
    public Response run(NDACRequest request) throws PMException, JSQLParserException, ClassNotFoundException, SQLException, InvalidEntityException, IOException {
        Graph graph = new MemGraphExt(new DummyGraphLoader());
        graph = buildGraph(graph, 1000);

        NDACResponse response = new NDACResponse();
        MemGraphExtSearch search = new MemGraphExtSearch((MemGraphExt) graph);
        HashSet<Node> nodes = search.search(null, NodeType.U.toString(), null);
        for(Node user : nodes) {
            // String originalSQL = "select employee_info.name, employee_info.ssn, employee_info.salary from employee_info";
            String originalSQL = request.getSql();
            Select select = (Select) CCJSqlParserUtil.parse(originalSQL);


            System.out.println("user: " + user.getName());
            NDACResponse.NDACResult userResult = new NDACResponse.NDACResult();
            userResult.setUser(user.getName());

            DatabaseContext ctx = new DatabaseContext("localhost", 3306, "root", "root", "employees");
            Algorithm algorithm = new SelectAlgorithm(select, user.getID(), 0, ctx, graph, search, null);

            // start timing the algorithm
            long start = System.nanoTime();
            double time;
            // translate the original sql into permitted sql
            String permittedSQL = algorithm.run();

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
        return ApiResponse.Builder
                .success()
                .entity(response)
                .build();
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
    public static Graph buildGraph(Graph graph, int numEmployees) throws DatabaseException, NullNameException, InvalidProhibitionSubjectTypeException, LoadConfigException, NullNodeException, NullTypeException, NoIDException, NodeNotFoundException, SessionDoesNotExistException, InvalidAssignmentException, MissingPermissionException, InvalidNodeTypeException, InvalidAssociationException {
        System.out.println("building graph");
        System.out.println("building employees pc");
        //create employees pc
        long pcID = graph.createNode(new Node(EMPLOYEES, PC, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEES)));
        long baseID = graph.createNode(new Node(EMPLOYEES, OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEES)));
        graph.assign(baseID, OA, pcID, PC);

        System.out.println("building employee_info oa");
        //create employee_info table
        long empInfoTableID = graph.createNode(new Node(EMPLOYEE_INFO, OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEES)));
        graph.assign(empInfoTableID, OA, baseID, OA);

        long rowsID = graph.createNode(new Node("rows", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        long colsID = graph.createNode(new Node("columns", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(rowsID, OA, empInfoTableID, OA);
        graph.assign(colsID, OA, empInfoTableID, OA);

        System.out.println("building employee_info column oas");
        //create column nodes
        long idID = graph.createNode(new Node("id", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO, "pk", "true", "schema_comp", "column")));
        graph.assign(idID, OA, colsID, OA);
        long nameID = graph.createNode(new Node("name", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO, "schema_comp", "column")));
        graph.assign(nameID, OA, colsID, OA);
        long ssnID = graph.createNode(new Node("ssn", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO, "schema_comp", "column")));
        graph.assign(ssnID, OA, colsID, OA);
        long salaryID = graph.createNode(new Node("salary", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO, "schema_comp", "column")));
        graph.assign(salaryID, OA, colsID, OA);

        System.out.println("building employee_info rows");
        //bob row
        long bobInfoID = graph.createNode(new Node("1", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(bobInfoID, OA, rowsID, OA);
        long oID = graph.createNode(new Node("bob_id", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, bobInfoID, OA);
        graph.assign(oID, O, idID, OA);
        oID = graph.createNode(new Node("bob_name", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, bobInfoID, OA);
        graph.assign(oID, O, nameID, OA);
        oID = graph.createNode(new Node("bob_ssn", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, bobInfoID, OA);
        graph.assign(oID, O, ssnID, OA);
        oID = graph.createNode(new Node("bob_salary", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, bobInfoID, OA);
        graph.assign(oID, O, salaryID, OA);

        //alice row
        long aliceInfoID = graph.createNode(new Node("2", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(aliceInfoID, OA, rowsID, OA);
        oID = graph.createNode(new Node("alice_id", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, aliceInfoID, OA);
        graph.assign(oID, O, idID, OA);
        oID = graph.createNode(new Node("alice_name", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, aliceInfoID, OA);
        graph.assign(oID, O, nameID, OA);
        oID = graph.createNode(new Node("alice_ssn", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, aliceInfoID, OA);
        graph.assign(oID, O, ssnID, OA);
        oID = graph.createNode(new Node("alice_salary", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, aliceInfoID, OA);
        graph.assign(oID, O, salaryID, OA);

        //charlie row
        long charlieInfoID = graph.createNode(new Node("3", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(charlieInfoID, OA, rowsID, OA);
        oID = graph.createNode(new Node("charlie_id", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, charlieInfoID, OA);
        graph.assign(oID, O, idID, OA);
        oID = graph.createNode(new Node("charlie_name", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, charlieInfoID, OA);
        graph.assign(oID, O, nameID, OA);
        oID = graph.createNode(new Node("charlie_ssn", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, charlieInfoID, OA);
        graph.assign(oID, O, ssnID, OA);
        oID = graph.createNode(new Node("charlie_salary", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, charlieInfoID, OA);
        graph.assign(oID, O, salaryID, OA);

        //dave row
        long daveInfoID = graph.createNode(new Node("4", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(daveInfoID, OA, rowsID, OA);
        oID = graph.createNode(new Node("dave_id", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, daveInfoID, OA);
        graph.assign(oID, O, idID, OA);
        oID = graph.createNode(new Node("dave_name", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, daveInfoID, OA);
        graph.assign(oID, O, nameID, OA);
        oID = graph.createNode(new Node("dave_ssn", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, daveInfoID, OA);
        graph.assign(oID, O, ssnID, OA);
        oID = graph.createNode(new Node("dave_salary", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(oID, O, daveInfoID, OA);
        graph.assign(oID, O, salaryID, OA);

        for (int i = 1; i <= numEmployees; i++) {
            System.out.println(String.valueOf(i + 4));
            long empID = graph.createNode(new Node(String.valueOf(i + 4), OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
            graph.assign(empID, OA, rowsID, OA);
            oID = graph.createNode(new Node(String.format("emp_%d_id", empID), O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
            graph.assign(oID, O, empID, OA);
            graph.assign(oID, O, idID, OA);
            oID = graph.createNode(new Node(String.format("emp_%d_name", empID), O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
            graph.assign(oID, O, empID, OA);
            graph.assign(oID, O, nameID, OA);
            oID = graph.createNode(new Node(String.format("emp_%d_ssn", empID), O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
            graph.assign(oID, O, empID, OA);
            graph.assign(oID, O, ssnID, OA);
            oID = graph.createNode(new Node(String.format("emp_%d_salary", empID), O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
            graph.assign(oID, O, empID, OA);
            graph.assign(oID, O, salaryID, OA);
        }

        System.out.println("building employee_contact oa");
        long empContTableID = graph.createNode(new Node(EMPLOYEE_CONTACT, OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(empContTableID, OA, baseID, OA);

        rowsID = graph.createNode(new Node("rows", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        colsID = graph.createNode(new Node("columns", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(rowsID, OA, empContTableID, OA);
        graph.assign(colsID, OA, empContTableID, OA);

        System.out.println("building employee_contact column oas");
        //create column nodes
        idID = graph.createNode(new Node("id", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT, "pk", "true", "schema_comp", "column")));
        graph.assign(idID, OA, colsID, OA);
        long phoneID = graph.createNode(new Node("phone", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT, "schema_comp", "column")));
        graph.assign(phoneID, OA, colsID, OA);
        long addressID = graph.createNode(new Node("address", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT, "schema_comp", "column")));
        graph.assign(addressID, OA, colsID, OA);

        System.out.println("building employee_contact rows");
        //bob row
        long bobContID = graph.createNode(new Node("1", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(bobContID, OA, rowsID, OA);
        oID = graph.createNode(new Node("bob_id", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(oID, O, bobContID, OA);
        graph.assign(oID, O, idID, OA);
        oID = graph.createNode(new Node("bob_phone", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(oID, O, bobContID, OA);
        graph.assign(oID, O, phoneID, OA);
        oID = graph.createNode(new Node("bob_address", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(oID, O, bobContID, OA);
        graph.assign(oID, O, addressID, OA);

        //alice row
        long aliceContID = graph.createNode(new Node("2", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(aliceContID, OA, rowsID, OA);
        oID = graph.createNode(new Node("alice_id", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(oID, O, aliceContID, OA);
        graph.assign(oID, O, idID, OA);
        oID = graph.createNode(new Node("alice_phone", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(oID, O, aliceContID, OA);
        graph.assign(oID, O, phoneID, OA);
        oID = graph.createNode(new Node("alice_address", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(oID, O, aliceContID, OA);
        graph.assign(oID, O, addressID, OA);

        //charlie row
        long charlieContID = graph.createNode(new Node("3", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(charlieContID, OA, rowsID, OA);
        oID = graph.createNode(new Node("charlie_id", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(oID, O, charlieContID, OA);
        graph.assign(oID, O, idID, OA);
        oID = graph.createNode(new Node("charlie_phone", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(oID, O, charlieContID, OA);
        graph.assign(oID, O, phoneID, OA);
        oID = graph.createNode(new Node("charlie_address", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(oID, O, charlieContID, OA);
        graph.assign(oID, O, addressID, OA);

        //dave row
        long daveContID = graph.createNode(new Node("4", OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(daveContID, OA, rowsID, OA);
        oID = graph.createNode(new Node("dave_id", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(oID, O, daveContID, OA);
        graph.assign(oID, O, idID, OA);
        oID = graph.createNode(new Node("dave_phone", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(oID, O, daveContID, OA);
        graph.assign(oID, O, phoneID, OA);
        oID = graph.createNode(new Node("dave_address", O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
        graph.assign(oID, O, daveContID, OA);
        graph.assign(oID, O, addressID, OA);

        for (int i = 1; i <= numEmployees; i++) {
            long empContID = graph.createNode(new Node(String.valueOf(i + 4), OA, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
            graph.assign(empContID, OA, rowsID, OA);
            oID = graph.createNode(new Node(String.format("emp_%d_id", empContID), O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
            graph.assign(oID, O, empContID, OA);
            graph.assign(oID, O, idID, OA);
            oID = graph.createNode(new Node(String.format("emp_%d_phone", empContID), O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
            graph.assign(oID, O, empContID, OA);
            graph.assign(oID, O, phoneID, OA);
            oID = graph.createNode(new Node(String.format("emp_%d_address", empContID), O, Node.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_CONTACT)));
            graph.assign(oID, O, empContID, OA);
            graph.assign(oID, O, addressID, OA);
        }

        System.out.println("creating users");
        //create users
        long bobID = graph.createNode(new Node("bob", U, null));
        long aliceID = graph.createNode(new Node("alice", U, null));
        long daveID = graph.createNode(new Node("dave", U, null));
        long charlieID = graph.createNode(new Node("charlie", U, null));

        long usersID = graph.createNode(new Node("users", UA, null));
        graph.assign(bobID, U, usersID, UA);
        graph.assign(aliceID, U, usersID, UA);
        graph.assign(daveID, U, usersID, UA);
        graph.assign(charlieID, U, usersID, UA);

        graph.assign(usersID, UA, pcID, PC);

        graph.associate(usersID, baseID, OA, new HashSet<>(Arrays.asList("read", "write")));

        System.out.println("configuring RBAC");
        //rbac
        pcID = graph.createNode(new Node("RBAC", PC, null));
        long empRecID = graph.createNode(new Node("records", OA, null));
        graph.assign(empRecID, OA, pcID, PC);
        long group1ID = graph.createNode(new Node("group 1 records", OA, null));
        graph.assign(group1ID, OA, empRecID, OA);
        graph.assign(bobInfoID, OA, group1ID, OA);
        graph.assign(bobContID, OA, group1ID, OA);
        graph.assign(daveInfoID, OA, group1ID, OA);
        graph.assign(daveContID, OA, group1ID, OA);
        long group2ID = graph.createNode(new Node("group 2 records", OA, null));
        graph.assign(group2ID, OA, empRecID, OA);
        graph.assign(aliceInfoID, OA, group2ID, OA);
        graph.assign(aliceContID, OA, group2ID, OA);
        long hrID = graph.createNode(new Node("hr records", OA, null));
        graph.assign(hrID, OA, empRecID, OA);
        graph.assign(charlieInfoID, OA, hrID, OA);
        graph.assign(charlieContID, OA, hrID, OA);

        long empsID = graph.createNode(new Node("employees", UA, null));
        graph.assign(empsID, UA, pcID, UA);
        long grp1UAID = graph.createNode(new Node("grp1", UA, null));
        long grp1MgrUAID = graph.createNode(new Node("grp1 mgr", UA, null));
        long grp2UAID = graph.createNode(new Node("grp2", UA, null));
        long grp2MgrUAID = graph.createNode(new Node("grp2 mgr", UA, null));
        long hrUAID = graph.createNode(new Node("hr", UA, null));
        graph.assign(grp1UAID, UA, empsID, UA);
        graph.assign(grp1MgrUAID, UA, grp1UAID, UA);
        graph.assign(grp2UAID, UA, empsID, UA);
        graph.assign(grp2MgrUAID, UA, grp2UAID, UA);
        graph.assign(hrUAID, UA, empsID, UA);

        graph.assign(bobID, U, grp1MgrUAID, UA);
        graph.assign(aliceID, U, grp2MgrUAID, UA);
        graph.assign(charlieID, U, hrUAID, UA);
        graph.assign(daveID, U, grp1UAID, UA);

        graph.associate(grp1UAID, group1ID, OA, new HashSet<>(Arrays.asList("read", "write")));
        graph.associate(grp2UAID, group2ID, OA, new HashSet<>(Arrays.asList("read", "write")));
        graph.associate(hrUAID, empRecID, OA, new HashSet<>(Arrays.asList("read", "write")));

        System.out.println("done!");

        return graph;
    }


}
