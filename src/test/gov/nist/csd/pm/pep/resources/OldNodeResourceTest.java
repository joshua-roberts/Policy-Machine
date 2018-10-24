/*
package gov.nist.csd.pm.pep.resources;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import gov.nist.csd.pm.model.exceptions.NodeNotFoundException;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.model.exceptions.ErrorCodes;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

class OldNodeResourceTest extends PMTest {

    private static final String NODES_URI = BASE_URI + "/nodes";


    */
/**
     * Check that the given ID is in the provided list.
     * @param list The list of nodes to check for the ID in.
     * @param id The ID to search for.
     * @throws NodeNotFoundException Thrown when the given ID is not in the list.
     *//*

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
    void getNodes() {
        LinkedTreeMap testGraph = buildTestGraph();

        // get all nodes
        Response response = given().queryParam("session", sessionID).get(NODES_URI);
        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.SUCCESS, res.getCode(),
                String.format("expected a success response code (9000) but received %s", res.getCode()));

        // check all the test nodes are there
        List list = (List) res.getEntity();
        try {
            checkForNode(list, idToLong(testGraph.get("uID")));
            checkForNode(list, idToLong(testGraph.get("uaID")));
            checkForNode(list, idToLong(testGraph.get("oID")));
            checkForNode(list, idToLong(testGraph.get("oaID")));
            checkForNode(list, idToLong(testGraph.get("pcID")));
        }
        catch (NodeNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }


        // get all oas
        response = given().queryParam("type", "OA").queryParam("session", sessionID).get(NODES_URI);
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.SUCCESS, res.getCode(),
                String.format("expected a success response code (9000) but received %s", res.getCode()));

        // check the test oa is there
        list = (List) res.getEntity();
        try {
            checkForNode(list, idToLong(testGraph.get("oaID")));
        }
        catch (NodeNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        // get all nodes with the namespace as a property
        response = given().queryParam("namespace", testGraph.get("uuid")).queryParam("session", sessionID).get(NODES_URI);
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.SUCCESS, res.getCode(),
                String.format("expected a success response code (9000) but received %s", res.getCode()));

        // check all test nodes are there
        list = (List) res.getEntity();
        try {
            checkForNode(list, idToLong(testGraph.get("uID")));
            checkForNode(list, idToLong(testGraph.get("oID")));
            checkForNode(list, idToLong(testGraph.get("uaID")));
            checkForNode(list, idToLong(testGraph.get("oaID")));
            checkForNode(list, idToLong(testGraph.get("pcID")));
        }
        catch (NodeNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void createPolicy() {
        //create policy class
        Response response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"name\": \"test_pc\",\"type\": \"PC\",\"properties\":{\"namespace\":\"" + uuid + "\"}}")
                .when()
                .post(NODES_URI + "/policies" + "?session=" + sessionID);
        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.SUCCESS, res.getCode(),
                String.format("expected a success response code (9000) but received %s", res.getCode()));
        LinkedTreeMap m = (LinkedTreeMap) res.getEntity();
        assertEquals(m.get("name"), "test_pc");

        //create policy class with same name different namespace property
        response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"name\": \"test_pc\",\"type\": \"PC\",\"properties\":{\"namespace\":\"" + uuid + "\",\"namespace\":\"test_pc\"}}")
                .when()
                .post(NODES_URI + "/policies" + "?session=" + sessionID);
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.ERR_POLICY_NAME_EXISTS, res.getCode());
    }

    @Test
    void getNode() {
        LinkedTreeMap testGraph = buildTestGraph();

        //test for existing node
        Response response = given().queryParam("session", sessionID).get(NODES_URI + "/{nodeID}", idToLong(testGraph.get("oaID")));
        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.SUCCESS, res.getCode(),
                String.format("expected a success response code (9000) but received %s", res.getCode()));
        LinkedTreeMap m = (LinkedTreeMap) res.getEntity();
        assertEquals(m.get("name"), "oa1");

        //test for non existing node
        response = given().queryParam("session", sessionID).get(NODES_URI + "/{nodeID}", -1);
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.ERR_NODE_NOT_FOUND, res.getCode());
    }

    @Test
    void deleteNode() {
        LinkedTreeMap testGraph = buildTestGraph();

        //test deleting object
        Response response = given().queryParam("session", sessionID).delete(NODES_URI + "/{nodeID}", idToLong(testGraph.get("oaID")));
        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.SUCCESS, res.getCode(),
                String.format("expected a success response code (9000) but received %s", res.getCode()));

        //test deleting non existent object
        response = given().queryParam("session", sessionID).delete(NODES_URI + "/{nodeID}", -1);
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.ERR_NODE_NOT_FOUND, res.getCode());
    }

    @Test
    void createNodeIn() {
        LinkedTreeMap testGraph = buildTestGraph();

        //create o in oa
        Response response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"name\": \"test_o\",\"type\": \"O\",\"properties\":{\"namespace\":\"" + uuid + "\"}}")
                .queryParam("session", sessionID)
                .when()
                .post(NODES_URI + "/{nodeID}/children", idToLong(testGraph.get("oaID")));
        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.SUCCESS, res.getCode(),
                String.format("expected a success response code (9000) but received %s", res.getCode()));


        //create o in non existing oa
        response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"name\": \"test_o1\",\"type\": \"O\",\"properties\":{\"namespace\":\"" + uuid + "\"}}")
                .queryParam("session", sessionID)
                .when()
                .post(NODES_URI + "/{nodeID}/children", -1);
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.ERR_NODE_NOT_FOUND, res.getCode());
        
        //create invalid assignment
        response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"name\": \"test_o1\",\"type\": \"O\",\"properties\":{\"namespace\":\"" + uuid + "\"}}")
                .queryParam("session", sessionID)
                .when()
                .post(NODES_URI + "/{nodeID}/children", idToLong(testGraph.get("uaID")));
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.ERR_INVALID_ASSIGNMENT, res.getCode());
    }

    @Test
    void getNodeChildren() {
        LinkedTreeMap testGraph = buildTestGraph();

        //get children of OA
        Response response = given().queryParam("session", sessionID).get(NODES_URI + "/{nodeID}/children", idToLong(testGraph.get("oaID")));
        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.SUCCESS, res.getCode(),
                String.format("expected a success response code (9000) but received %s", res.getCode()));

        // check all the test nodes are there
        List list = (List) res.getEntity();
        try {
            checkForNode(list, idToLong(testGraph.get("oID")));
        }
        catch (NodeNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        //get children of non existing node
        response = given().queryParam("session", sessionID).get(NODES_URI + "/{nodeID}/children", -1);
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.ERR_NODE_NOT_FOUND, res.getCode());
    }

    @Test
    void deleteNodeChildren() {
        LinkedTreeMap testGraph = buildTestGraph();

        //delete children of OA
        Response response = given().queryParam("session", sessionID).delete(NODES_URI + "/{nodeID}/children", idToLong(testGraph.get("oaID")));
        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.SUCCESS, res.getCode(),
                String.format("expected a success response code (9000) but received %s", res.getCode()));

        //check that the object has been deleted from oa children
        response = given().queryParam("session", sessionID).get(NODES_URI + "/{nodeID}/children", idToLong(testGraph.get("oaID")));
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.SUCCESS, res.getCode(),
                String.format("expected a success response code (9000) but received %s", res.getCode()));

        List list = (List) res.getEntity();
        try {
            checkForNode(list, idToLong(testGraph.get("oID")));
            fail("object o1 was not deleted from the children of oa1");
        }
        catch (NodeNotFoundException e) {
            // an exception here means the object was successfully deleted from the children
        }

        //delete children of non existing node
        response = given().queryParam("session", sessionID).delete(NODES_URI + "/{nodeID}/children", -1);
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.ERR_NODE_NOT_FOUND, res.getCode());
    }

    @Test
    void getNodeParents() {
        LinkedTreeMap testGraph = buildTestGraph();

        //get children of OA
        Response response = given().queryParam("session", sessionID).get(NODES_URI + "/{nodeID}/parents", idToLong(testGraph.get("oID")));
        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.SUCCESS, res.getCode(),
                String.format("expected a success response code (9000) but received %s", res.getCode()));

        // check all the test nodes are there
        List list = (List) res.getEntity();
        try {
            checkForNode(list, idToLong(testGraph.get("oaID")));
        }
        catch (NodeNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        //get children of non existing node
        response = given().queryParam("session", sessionID).get(NODES_URI + "/{nodeID}/parents", -1);
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ErrorCodes.ERR_NODE_NOT_FOUND, res.getCode());
    }
}*/
