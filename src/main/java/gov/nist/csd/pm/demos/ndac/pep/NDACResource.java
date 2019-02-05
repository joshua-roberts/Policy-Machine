package gov.nist.csd.pm.demos.ndac.pep;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.Search;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.common.model.graph.nodes.NodeUtils;
import gov.nist.csd.pm.common.model.prohibitions.Prohibition;
import gov.nist.csd.pm.demos.ndac.algorithms.parsing.v1.Algorithm;
import gov.nist.csd.pm.demos.ndac.algorithms.parsing.v1.SelectAlgorithm;
import gov.nist.csd.pm.demos.ndac.algorithms.parsing.v2.AlgorithmV2;
import gov.nist.csd.pm.demos.ndac.algorithms.parsing.v2.SelectAlgorithmV2;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.db.sql.SQLConnection;
import gov.nist.csd.pm.pap.graph.MemGraph;
import gov.nist.csd.pm.pap.graph.Neo4jGraph;
import gov.nist.csd.pm.pap.loader.graph.DummyGraphLoader;
import gov.nist.csd.pm.pap.search.MemGraphSearch;
import gov.nist.csd.pm.pdp.engine.Decider;
import gov.nist.csd.pm.pdp.engine.PReviewDecider;
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
import java.util.concurrent.ThreadLocalRandom;

import static gov.nist.csd.pm.common.constants.Properties.NAMESPACE_PROPERTY;
import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.*;

@Path("/demos/ndac")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NDACResource {

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

    private NDACResponse runParsing(String version, NDACRequest request, Graph graph, List<Prohibition> prohibitions) throws JSQLParserException, PMException, ClassNotFoundException, SQLException, IOException {
        NDACResponse response = new NDACResponse();
        MemGraphSearch search = new MemGraphSearch((MemGraph) graph);
        HashSet<NodeContext> nodes = search.search(null, NodeType.U.toString(), null);
        for(NodeContext user : nodes) {
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

            HashSet<NodeContext> parents = graph.getParents(user.getID());
            List<String> attrs = new ArrayList<>();
            for(NodeContext node : parents) {
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
    public static NGACContext buildGraph(Graph graph, int numEmployees) throws PMException {
        //create employees pc
        long pcID = graph.createNode(new NodeContext(getID(), EMPLOYEES, PC, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEES)));
        long baseID = graph.createNode(new NodeContext(getID(), EMPLOYEES, OA, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEES)));
        graph.assign(new NodeContext(baseID, OA), new NodeContext(pcID, PC));

        //create employee_info table
        long empInfoTableID = graph.createNode(new NodeContext(getID(), EMPLOYEE_INFO, OA, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEES)));
        graph.assign(new NodeContext(empInfoTableID, OA), new NodeContext(baseID, OA));

        long rowsID = graph.createNode(new NodeContext(getID(), "rows", OA, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        long colsID = graph.createNode(new NodeContext(getID(), "columns", OA, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(rowsID, OA), new NodeContext(empInfoTableID, OA));
        graph.assign(new NodeContext(colsID, OA), new NodeContext(empInfoTableID, OA));

        //create column nodes
        long idID = graph.createNode(new NodeContext(getID(), "id", OA, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO, "pk", "true", "schema_comp", "column")));
        graph.assign(new NodeContext(idID, OA), new NodeContext(colsID, OA));
        long nameID = graph.createNode(new NodeContext(getID(), "name", OA, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO, "schema_comp", "column")));
        graph.assign(new NodeContext(nameID, OA), new NodeContext(colsID, OA));
        long ssnID = graph.createNode(new NodeContext(getID(), "ssn", OA, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO, "schema_comp", "column")));
        graph.assign(new NodeContext(ssnID, OA), new NodeContext(colsID, OA));
        long salaryID = graph.createNode(new NodeContext(getID(), "salary", OA, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO, "schema_comp", "column")));
        graph.assign(new NodeContext(salaryID, OA), new NodeContext(colsID, OA));


        List<Long> group1Records = new ArrayList<>();
        List<Long> group2Records = new ArrayList<>();

        //bob row
        long bobInfoID = graph.createNode(new NodeContext(getID(), "1", OA, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(bobInfoID, OA), new NodeContext(rowsID, OA));
        group1Records.add(bobInfoID);
        long bob_id = graph.createNode(new NodeContext(getID(), "bob_id", O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(bob_id, O), new NodeContext(bobInfoID, OA));
        graph.assign(new NodeContext(bob_id, O), new NodeContext(idID, OA));
        long bob_name = graph.createNode(new NodeContext(getID(), "bob_name", O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(bob_name, O), new NodeContext(bobInfoID, OA));
        graph.assign(new NodeContext(bob_name, O), new NodeContext(nameID, OA));
        long bob_ssn = graph.createNode(new NodeContext(getID(), "bob_ssn", O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(bob_ssn, O), new NodeContext(bobInfoID, OA));
        graph.assign(new NodeContext(bob_ssn, O), new NodeContext(ssnID, OA));
        long bob_salary = graph.createNode(new NodeContext(getID(), "bob_salary", O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(bob_salary, O), new NodeContext(bobInfoID, OA));
        graph.assign(new NodeContext(bob_salary, O), new NodeContext(salaryID, OA));

        //alice row
        long aliceInfoID = graph.createNode(new NodeContext(getID(), "2", OA, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(aliceInfoID, OA), new NodeContext(rowsID, OA));
        group2Records.add(aliceInfoID);
        long alice_id = graph.createNode(new NodeContext(getID(), "alice_id", O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(alice_id, O), new NodeContext(aliceInfoID, OA));
        graph.assign(new NodeContext(alice_id, O), new NodeContext(idID, OA));
        long alice_name = graph.createNode(new NodeContext(getID(), "alice_name", O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(alice_name, O), new NodeContext(aliceInfoID, OA));
        graph.assign(new NodeContext(alice_name, O), new NodeContext(nameID, OA));
        long alice_ssn = graph.createNode(new NodeContext(getID(), "alice_ssn", O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(alice_ssn, O), new NodeContext(aliceInfoID, OA));
        graph.assign(new NodeContext(alice_ssn, O), new NodeContext(ssnID, OA));
        long alice_salary = graph.createNode(new NodeContext(getID(), "alice_salary", O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(alice_salary, O), new NodeContext(aliceInfoID, OA));
        graph.assign(new NodeContext(alice_salary, O), new NodeContext(salaryID, OA));

        //charlie row
        long charlieInfoID = graph.createNode(new NodeContext(getID(), "3", OA, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(charlieInfoID, OA), new NodeContext(rowsID, OA));
        long charlie_id = graph.createNode(new NodeContext(getID(), "charlie_id", O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(charlie_id, O), new NodeContext(charlieInfoID, OA));
        graph.assign(new NodeContext(charlie_id, O), new NodeContext(idID, OA));
        long charlie_name = graph.createNode(new NodeContext(getID(), "charlie_name", O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(charlie_name, O), new NodeContext(charlieInfoID, OA));
        graph.assign(new NodeContext(charlie_name, O), new NodeContext(nameID, OA));
        long charlie_ssn = graph.createNode(new NodeContext(getID(), "charlie_ssn", O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(charlie_ssn, O), new NodeContext(charlieInfoID, OA));
        graph.assign(new NodeContext(charlie_ssn, O), new NodeContext(ssnID, OA));
        long charlie_salary = graph.createNode(new NodeContext(getID(), "charlie_salary", O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(charlie_salary, O), new NodeContext(charlieInfoID, OA));
        graph.assign(new NodeContext(charlie_salary, O), new NodeContext(salaryID, OA));

        //dave row
        long daveInfoID = graph.createNode(new NodeContext(getID(), "4", OA, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(daveInfoID, OA), new NodeContext(rowsID, OA));
        group1Records.add(daveInfoID);
        long dave_id = graph.createNode(new NodeContext(getID(), "dave_id", O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(dave_id, O), new NodeContext(daveInfoID, OA));
        graph.assign(new NodeContext(dave_id, O), new NodeContext(idID, OA));
        long dave_name = graph.createNode(new NodeContext(getID(), "dave_name", O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(dave_name, O), new NodeContext(daveInfoID, OA));
        graph.assign(new NodeContext(dave_name, O), new NodeContext(nameID, OA));
        long dave_ssn = graph.createNode(new NodeContext(getID(), "dave_ssn", O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(dave_ssn, O), new NodeContext(daveInfoID, OA));
        graph.assign(new NodeContext(dave_ssn, O), new NodeContext(ssnID, OA));
        long dave_salary = graph.createNode(new NodeContext(getID(), "dave_salary", O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
        graph.assign(new NodeContext(dave_salary, O), new NodeContext(daveInfoID, OA));
        graph.assign(new NodeContext(dave_salary, O), new NodeContext(salaryID, OA));

        pcID = graph.createNode(new NodeContext(getID(), "ndac", PC, null));

        long grp1SalID = graph.createNode(new NodeContext(getID(), "Grp1Sal", OA, null));
        long grp2SalID = graph.createNode(new NodeContext(getID(), "Grp2Sal", OA, null));
        graph.assign(new NodeContext(grp1SalID, OA), new NodeContext(pcID, PC));
        graph.assign(new NodeContext(grp2SalID, OA), new NodeContext(pcID, PC));

        // assign users' salaries
        graph.assign(new NodeContext(bob_salary, O), new NodeContext(grp1SalID, OA));
        graph.assign(new NodeContext(dave_salary, O), new NodeContext(grp1SalID, OA));
        graph.assign(new NodeContext(alice_salary, O), new NodeContext(grp2SalID, OA));

        for (int i = 1; i <= numEmployees; i++) {
            System.out.println(String.valueOf(i + 4));
            long empID = graph.createNode(new NodeContext(getID(), String.valueOf(i + 4), OA, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
            graph.assign(new NodeContext(empID, OA), new NodeContext(rowsID, OA));
            long oID = graph.createNode(new NodeContext(getID(), String.format("emp_%d_id", empID), O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
            graph.assign(new NodeContext(oID, O), new NodeContext(empID, OA));
            graph.assign(new NodeContext(oID, O), new NodeContext(idID, OA));
            oID = graph.createNode(new NodeContext(getID(), String.format("emp_%d_name", empID), O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
            graph.assign(new NodeContext(oID, O), new NodeContext(empID, OA));
            graph.assign(new NodeContext(oID, O), new NodeContext(nameID, OA));
            oID = graph.createNode(new NodeContext(getID(), String.format("emp_%d_ssn", empID), O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
            graph.assign(new NodeContext(oID, O), new NodeContext(empID, OA));
            graph.assign(new NodeContext(oID, O), new NodeContext(ssnID, OA));
            oID = graph.createNode(new NodeContext(getID(), String.format("emp_%d_salary", empID), O, NodeUtils.toProperties(NAMESPACE_PROPERTY, EMPLOYEE_INFO)));
            graph.assign(new NodeContext(oID, O), new NodeContext(empID, OA));
            graph.assign(new NodeContext(oID, O), new NodeContext(salaryID, OA));

            if(i%2 == 0) {
                group1Records.add(empID);
                graph.assign(new NodeContext(oID, OA), new NodeContext(grp1SalID, OA));
            } else {
                group2Records.add(empID);
                graph.assign(new NodeContext(oID, OA), new NodeContext(grp2SalID, OA));
            }
        }

        // user attributes
        long hrID = graph.createNode(new NodeContext(getID(), "HR", UA, null));
        long grp1MgrID = graph.createNode(new NodeContext(getID(), "Grp1Mgr", UA, null));
        long grp2MgrID = graph.createNode(new NodeContext(getID(), "Grp2Mgr", UA, null));
        long staffID = graph.createNode(new NodeContext(getID(), "Staff", UA, null));
        long bobUAID = graph.createNode(new NodeContext(getID(), "Bob", UA, null));
        long aliceUAID = graph.createNode(new NodeContext(getID(), "Alice", UA, null));
        long daveUAID = graph.createNode(new NodeContext(getID(), "Dave", UA, null));
        long charlieUAID = graph.createNode(new NodeContext(getID(), "Charlie", UA, null));

        // assign oas to pc
        graph.assign(new NodeContext(salaryID, OA), new NodeContext(pcID, PC));
        graph.assign(new NodeContext(ssnID, OA), new NodeContext(pcID, PC));
        graph.assign(new NodeContext(grp1SalID, OA), new NodeContext(pcID, PC));
        graph.assign(new NodeContext(grp2SalID, OA), new NodeContext(pcID, PC));

        System.out.println("creating users");
        //create users
        long bobID = graph.createNode(new NodeContext(getID(), "bob", U, null));
        long aliceID = graph.createNode(new NodeContext(getID(), "alice", U, null));
        long daveID = graph.createNode(new NodeContext(getID(), "dave", U, null));
        long charlieID = graph.createNode(new NodeContext(getID(), "charlie", U, null));

        long usersID = graph.createNode(new NodeContext(getID(), "users", UA, null));
        graph.assign(new NodeContext(bobID, U), new NodeContext(usersID, UA));
        graph.assign(new NodeContext(aliceID, U), new NodeContext(usersID, UA));
        graph.assign(new NodeContext(daveID, U), new NodeContext(usersID, UA));
        graph.assign(new NodeContext(charlieID, U), new NodeContext(usersID, UA));

        graph.assign(new NodeContext(usersID, UA), new NodeContext(pcID, PC));

        graph.associate(new NodeContext(usersID, UA), new NodeContext(baseID, OA), new HashSet<>(Arrays.asList("read")));

        //hr
        graph.assign(new NodeContext(charlieID, U), new NodeContext(hrID, UA));
        graph.associate(new NodeContext(hrID, UA), new NodeContext(ssnID, OA), new HashSet<>(Arrays.asList("read", "write")));
        graph.associate(new NodeContext(hrID, UA), new NodeContext(salaryID, OA), new HashSet<>(Arrays.asList("read", "write")));

        //Grp1Mgr
        graph.assign(new NodeContext(bobID, U), new NodeContext(grp1MgrID, UA));
        graph.associate(new NodeContext(grp1MgrID, UA), new NodeContext(grp1SalID, OA), new HashSet<>(Arrays.asList("read")));
        //Grp2Mgr
        graph.assign(new NodeContext(aliceID, U), new NodeContext(grp2MgrID, UA));
        graph.associate(new NodeContext(grp2MgrID, UA), new NodeContext(grp2SalID, OA), new HashSet<>(Arrays.asList("read")));

        //staff
        graph.assign(new NodeContext(bobID, U), new NodeContext(staffID, UA));
        graph.assign(new NodeContext(aliceID, U), new NodeContext(staffID, UA));
        graph.assign(new NodeContext(daveID, U), new NodeContext(staffID, UA));
        graph.assign(new NodeContext(charlieID, U), new NodeContext(staffID, UA));
        // assign name column to public container
        long publicID = graph.createNode(new NodeContext(getID(), "Public", OA, null));
        graph.assign(new NodeContext(publicID, OA), new NodeContext(pcID, PC));
        graph.assign(new NodeContext(nameID, OA), new NodeContext(publicID, OA));

        graph.associate(new NodeContext(staffID, UA), new NodeContext(publicID, OA), new HashSet<>(Arrays.asList("read")));

        //bob
        graph.assign(new NodeContext(bobID, U), new NodeContext(bobUAID, UA));
        graph.assign(new NodeContext(bobInfoID, OA), new NodeContext(pcID, PC));
        graph.associate(new NodeContext(bobUAID, UA), new NodeContext(bobInfoID, OA), new HashSet<>(Arrays.asList("read", "write")));
        //alice
        graph.assign(new NodeContext(aliceID, U), new NodeContext(aliceUAID, UA));
        graph.assign(new NodeContext(aliceInfoID, OA), new NodeContext(pcID, PC));
        graph.associate(new NodeContext(aliceUAID, UA), new NodeContext(aliceInfoID, OA), new HashSet<>(Arrays.asList("read", "write")));
        //dave
        graph.assign(new NodeContext(daveID, U), new NodeContext(daveUAID, UA));
        graph.assign(new NodeContext(daveInfoID, OA), new NodeContext(pcID, PC));
        graph.associate(new NodeContext(daveUAID, UA), new NodeContext(daveInfoID, OA), new HashSet<>(Arrays.asList("read", "write")));
        //charlie
        graph.assign(new NodeContext(charlieID, U), new NodeContext(charlieUAID, UA));
        graph.assign(new NodeContext(charlieInfoID, OA), new NodeContext(pcID, PC));
        graph.associate(new NodeContext(charlieUAID, UA), new NodeContext(charlieInfoID, OA), new HashSet<>(Arrays.asList("read", "write")));

        /*System.out.println("configuring RBAC");
        System.out.println(group1Records);
        System.out.println(group2Records);

        //rbac
        pcID = graph.createNode(new NodeContext(getID(), "RBAC", PC, null));
        //records
        long empRecID = graph.createNode(new NodeContext(getID(), "records", OA, null));
        graph.assign(new NodeContext(empRecID, OA), new NodeContext(pcID, PC));
        //group 1 records
        long group1ID = graph.createNode(new NodeContext(getID(), "group 1 records", OA, null));
        graph.assign(new NodeContext(group1ID, OA), new NodeContext(empRecID, OA));
        for(long recID : group1Records) {
            graph.assign(new NodeContext(recID, OA), new NodeContext(group1ID, OA));
        }
        //group 2 records
        long group2ID = graph.createNode(new NodeContext(getID(), "group 2 records", OA, null));
        graph.assign(new NodeContext(group2ID, OA), new NodeContext(empRecID, OA));
        for(long recID : group2Records) {
            graph.assign(new NodeContext(recID, OA), new NodeContext(group2ID, OA));
        }
        long hrID = graph.createNode(new NodeContext(getID(), "hr records", OA, null));
        graph.assign(new NodeContext(hrID, OA), new NodeContext(empRecID, OA));
        graph.assign(new NodeContext(charlieInfoID, OA), new NodeContext(hrID, OA));

        // group uas
        long empsID = graph.createNode(new NodeContext(getID(), "employees", UA, null));
        graph.assign(new NodeContext(empsID, UA), new NodeContext(pcID, PC));
        long grp1UA = graph.createNode(new NodeContext(getID(), "grp1", UA, null));
        long grp2UA = graph.createNode(new NodeContext(getID(), "grp2", UA, null));
        long hrUAID = graph.createNode(new NodeContext(getID(), "hr", UA, null));
        graph.assign(new NodeContext(hrUAID, UA), new NodeContext(empsID, UA));
        graph.assign(new NodeContext(grp1UA, UA), new NodeContext(empsID, UA));
        graph.assign(new NodeContext(grp2UA, UA), new NodeContext(empsID, UA));

        graph.assign(new NodeContext(bobID, U), new NodeContext(grp1UA, UA));
        graph.assign(new NodeContext(aliceID, U), new NodeContext(grp2UA, UA));
        graph.assign(new NodeContext(charlieID, U), new NodeContext(hrUAID, UA));
        graph.assign(new NodeContext(daveID, U), new NodeContext(grp1UA, UA));

        graph.associate(new NodeContext(grp1UA, UA), new NodeContext(group1ID, OA), "read")));
        graph.associate(new NodeContext(grp2UA, UA), new NodeContext(group2ID, OA), "read")));
        graph.associate(new NodeContext(hrUAID, UA), new NodeContext(empRecID, OA), "read")));

        //homes
        long bobHomeID = graph.createNode(new NodeContext(getID(), "bob home", OA, null));
        long aliceHomeID = graph.createNode(new NodeContext(getID(), "alice home", OA, null));
        long daveHomeID = graph.createNode(new NodeContext(getID(), "dave home", OA, null));
        long charlieHomeID = graph.createNode(new NodeContext(getID(), "charlie home", OA, null));
        graph.assign(new NodeContext(bobHomeID, OA), new NodeContext(pcID, PC));
        graph.assign(new NodeContext(aliceHomeID, OA), new NodeContext(pcID, PC));
        graph.assign(new NodeContext(daveHomeID, OA), new NodeContext(pcID, PC));
        graph.assign(new NodeContext(charlieHomeID, OA), new NodeContext(pcID, PC));

        graph.assign(new NodeContext(group1ID, OA), new NodeContext(bobHomeID, OA));
        graph.assign(new NodeContext(group2ID, OA), new NodeContext(aliceHomeID, OA));
        graph.assign(new NodeContext(empRecID, OA), new NodeContext(charlieHomeID, OA));
        graph.assign(new NodeContext(daveInfoID, OA), new NodeContext(daveHomeID, OA));

        long uaID = graph.createNode(new NodeContext(getID(), "Bob", UA, null));
        graph.assign(new NodeContext(bobID, U), new NodeContext(uaID, UA));
        graph.associate(new NodeContext(uaID, UA), new NodeContext(bobHomeID, OA), "read")));
        //graph.assign(new NodeContext(group1ID, OA, bobHomeID, OA));

        uaID = graph.createNode(new NodeContext(getID(), "Alice", UA, null));
        graph.assign(new NodeContext(aliceID, U), new NodeContext(uaID, UA));
        graph.associate(new NodeContext(uaID, UA), new NodeContext(aliceHomeID, OA), "read")));
        //graph.assign(new NodeContext(group2ID, OA, aliceHomeID, OA));

        uaID = graph.createNode(new NodeContext(getID(), "Dave", UA, null));
        graph.assign(new NodeContext(daveID, U), new NodeContext(uaID, UA));
        graph.associate(new NodeContext(uaID, UA), new NodeContext(daveHomeID, OA), "read")));
        graph.assign(new NodeContext(daveInfoID, OA), new NodeContext(daveHomeID, OA));

        uaID = graph.createNode(new NodeContext(getID(), "Charlie", UA, null));
        graph.assign(new NodeContext(charlieID, U), new NodeContext(uaID, UA));
        graph.associate(new NodeContext(uaID, UA), new NodeContext(charlieHomeID, OA), "read")));
        //graph.assign(new NodeContext(empRecID, OA, charlieHomeID, OA));
*/
        List<Prohibition> prohibitions = new ArrayList<>();
        //uncomment the below code to add prohibitions
        //prohibitions
        /*
        //bob - ssn
        Prohibition prohibition = new Prohibition();
        prohibition.setName("bob_deny_ssn");
        prohibition.setSubject(new ProhibitionSubject(bobID, ProhibitionSubjectType.U));
        prohibition.setIntersection(true);
        prohibition.setOperations("read")));
        prohibition.addNode(new ProhibitionNode(ssnID, false));
        prohibition.addNode(new ProhibitionNode(bobInfoID, true));
        prohibitions.add(prohibition);
        //alice ssn
        prohibition = new Prohibition();
        prohibition.setName("alice_deny_ssn");
        prohibition.setSubject(new ProhibitionSubject(aliceID, ProhibitionSubjectType.U));
        prohibition.setIntersection(true);
        prohibition.setOperations("read")));
        prohibition.addNode(new ProhibitionNode(ssnID, false));
        prohibition.addNode(new ProhibitionNode(aliceInfoID, true));
        prohibitions.add(prohibition);

        // dave ssn and salary
        prohibition = new Prohibition();
        prohibition.setName("dave_deny_ssn");
        prohibition.setSubject(new ProhibitionSubject(daveID, ProhibitionSubjectType.U));
        prohibition.setIntersection(true);
        prohibition.setOperations("read")));
        prohibition.addNode(new ProhibitionNode(ssnID, false));
        prohibition.addNode(new ProhibitionNode(daveInfoID, true));
        prohibitions.add(prohibition);
        prohibition = new Prohibition();
        prohibition.setName("dave_deny_salary");
        prohibition.setSubject(new ProhibitionSubject(daveID, ProhibitionSubjectType.U));
        prohibition.setIntersection(true);
        prohibition.setOperations("read")));
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
        Graph graph;
        List<Prohibition> prohibitions;

        public NGACContext(Graph graph, List<Prohibition> prohibitions) {
            this.graph = graph;
            this.prohibitions = prohibitions;
        }

        public Graph getGraph() {
            return graph;
        }

        public List<Prohibition> getProhibitions() {
            return prohibitions;
        }
    }
}
