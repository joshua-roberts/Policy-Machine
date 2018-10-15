package gov.nist.csd.pm.pep.resources;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidPropertyException;
import gov.nist.csd.pm.model.exceptions.NodeNotFoundException;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.graph.NodeType;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pip.dao.DAOManager;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static gov.nist.csd.pm.model.Constants.NEW_NODE_ID;
import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

class NodeResourceTest extends PMTest {

    private static final String URI = BASE_URI + "/nodes";

    class TestGraph {
        String uuid;
        long oID, uID, uaID, oaID, pcID;

        public TestGraph(String uuid, long oID, long uID, long uaID, long oaID, long pcID) {
            this.uuid = uuid;
            this.oID = oID;
            this.uID = uID;
            this.uaID = uaID;
            this.oaID = oaID;
            this.pcID = pcID;
        }
    }

    private long createNode(String sessionID, long parentID, String name, String type, String uuid) {
        String uri;
        if (type.equalsIgnoreCase("pc")) {
            uri = "/policies";
        } else {
            uri = String.format("/%d/children", parentID);
        }
        Response response = given().
                contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"name\": \"" + name + "\",\"type\": \"" + type + "\",\"properties\":{\"uuid\":\"" + uuid + "\"}}")
                .when()
                .post(URI + uri + "?session=" + sessionID);

        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        LinkedTreeMap m = (LinkedTreeMap) res.getEntity();
        return (long)((double)m.get("id"));
    }

    private TestGraph buildTestGraph() throws ClassNotFoundException, SQLException, DatabaseException, InvalidPropertyException, IOException {
        String uuid = UUID.randomUUID().toString();
        Map<String, String> properties = new HashMap<>();
        properties.put("uuid", uuid);

        Node pc = DAOManager.getDaoManager().getNodesDAO().createNode(NEW_NODE_ID, "pc1", NodeType.PC, properties);
        Node oa = DAOManager.getDaoManager().getNodesDAO().createNode(NEW_NODE_ID, "oa1", NodeType.OA, properties);
        Node ua = DAOManager.getDaoManager().getNodesDAO().createNode(NEW_NODE_ID, "ua1", NodeType.UA, properties);
        Node u = DAOManager.getDaoManager().getNodesDAO().createNode(NEW_NODE_ID, "u1", NodeType.U, properties);
        Node o = DAOManager.getDaoManager().getNodesDAO().createNode(NEW_NODE_ID, "o1", NodeType.O, properties);

        return new TestGraph(uuid, u.getID(), o.getID(), ua.getID(), oa.getID(), pc.getID());
    }

    private void tearDown(TestGraph testGraph) throws ClassNotFoundException, SQLException, DatabaseException, InvalidPropertyException, IOException {
        System.out.println("cleaning up");

        System.out.println("deleting nodes with uuid = " + testGraph.uuid);
        DAOManager.getDaoManager().getNodesDAO().deleteNode(testGraph.uID);
        DAOManager.getDaoManager().getNodesDAO().deleteNode(testGraph.uaID);
        DAOManager.getDaoManager().getNodesDAO().deleteNode(testGraph.oID);
        DAOManager.getDaoManager().getNodesDAO().deleteNode(testGraph.oaID);
        DAOManager.getDaoManager().getNodesDAO().deleteNode(testGraph.pcID);
    }

    /**
     * Check that the given ID is in the provided list.
     * @param list The list of nodes to check for the ID in.
     * @param id The ID to search for.
     * @throws NodeNotFoundException Thrown when the given ID is not in the list.
     */
    private void checkForNode(List list, long id) throws NodeNotFoundException {
        for (Object o : list) {
            LinkedTreeMap m = (LinkedTreeMap) o;
            long foundID = (long)((double)m.get("id"));
            if (foundID == id) {
                return;
            }
        }

        throw new NodeNotFoundException(id);
    }

    @Test
    void getNodes() throws DatabaseException {
        TestGraph testGraph = null;
        try {
            testGraph = buildTestGraph();
        }
        catch (ClassNotFoundException | SQLException |
                InvalidPropertyException | IOException e) {
            e.printStackTrace();
            fail("there was an error building the test graph: " + e.getMessage());
        }

        String sessionID = getSessionID();

        // get all nodes
        Response response = given().queryParam("session", sessionID).get(URI);
        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        response.then().body("code", Matchers.is(9000));

        // check all the test nodes are there
        List list = (List) res.getEntity();
        try {
            checkForNode(list, testGraph.uID);
            checkForNode(list, testGraph.uaID);
            checkForNode(list, testGraph.oID);
            checkForNode(list, testGraph.oaID);
            checkForNode(list, testGraph.pcID);
        }
        catch (NodeNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }


        // get all oas
        response = given().queryParam("type", "OA").queryParam("session", sessionID).get(URI);
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        response.then().body("code", Matchers.is(9000));

        // check the test oa is there
        list = (List) res.getEntity();
        try {
            checkForNode(list, testGraph.oaID);
        }
        catch (NodeNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        // get all nodes with the uuid as a property
        response = given().queryParam("uuid", testGraph.uuid).queryParam("session", sessionID).get(URI);
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        response.then().body("code", Matchers.is(9000));

        // check all test nodes are there
        list = (List) res.getEntity();
        try {
            checkForNode(list, testGraph.uID);
            checkForNode(list, testGraph.uaID);
            checkForNode(list, testGraph.oID);
            checkForNode(list, testGraph.oaID);
            checkForNode(list, testGraph.pcID);
        }
        catch (NodeNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        //tear down the test graph
        try {
            tearDown(testGraph);
        }
        catch (ClassNotFoundException | SQLException |
                IOException | InvalidPropertyException e) {
            e.printStackTrace();
            fail("there was an error tearing down the test graph: " + e.getMessage());
        }
    }

    @Test
    void createPolicy() {

    }

    @Test
    void getNode() {
    }

    @Test
    void deleteNode() {
    }

    @Test
    void createNodeIn() {
    }

    @Test
    void getNodeChildren() {
    }

    @Test
    void deleteNodeChildren() {
    }

    @Test
    void getNodeParents() {
    }
}