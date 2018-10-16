package gov.nist.csd.pm.pep.resources;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidPropertyException;
import gov.nist.csd.pm.model.exceptions.NodeNotFoundException;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.graph.NodeType;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pep.response.ApiResponseCodes;
import gov.nist.csd.pm.pip.dao.DAOManager;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.csd.pm.model.Constants.NEW_NODE_ID;
import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

class NodeResourceTest extends PMTest {

    private static final String URI = BASE_URI + "/nodes";
    private static final String TEST_URI = BASE_URI + "/tests/graph";
    private LinkedTreeMap testGraphIDs;

    @BeforeEach
    void buildTestGraph() {
        String sessionID = getSessionID();

        Response response = given().queryParam("session", sessionID).get(TEST_URI);
        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        response.then().body("code", Matchers.is(ApiResponseCodes.SUCCESS));

        testGraphIDs = (LinkedTreeMap) res.getEntity();
    }

    @AfterEach
    void tearDown() {
        Response response = given().delete(TEST_URI + "/" + testGraphIDs.get("uuid"));
        response.then().body("code", Matchers.is(ApiResponseCodes.SUCCESS));
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
    void getNodes() {
        String sessionID = getSessionID();

        // get all nodes
        Response response = given().queryParam("session", sessionID).get(URI);
        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        response.then().body("code", Matchers.is(ApiResponseCodes.SUCCESS));

        // check all the test nodes are there
        List list = (List) res.getEntity();
        try {
            checkForNode(list, (long)((double)testGraphIDs.get("uID")));
            checkForNode(list, (long)((double)testGraphIDs.get("uaID")));
            checkForNode(list, (long)((double)testGraphIDs.get("oID")));
            checkForNode(list, (long)((double)testGraphIDs.get("oaID")));
            checkForNode(list, (long)((double)testGraphIDs.get("pcID")));
        }
        catch (NodeNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }


        // get all oas
        response = given().queryParam("type", "OA").queryParam("session", sessionID).get(URI);
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        response.then().body("code", Matchers.is(ApiResponseCodes.SUCCESS));

        // check the test oa is there
        list = (List) res.getEntity();
        try {
            checkForNode(list, (long)((double)testGraphIDs.get("oaID")));
        }
        catch (NodeNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        // get all nodes with the uuid as a property
        response = given().queryParam("uuid", testGraphIDs.get("uuid")).queryParam("session", sessionID).get(URI);
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        response.then().body("code", Matchers.is(ApiResponseCodes.SUCCESS));

        // check all test nodes are there
        list = (List) res.getEntity();
        try {
            checkForNode(list, (long)((double)testGraphIDs.get("uID")));
            checkForNode(list, (long)((double)testGraphIDs.get("oID")));
            checkForNode(list, (long)((double)testGraphIDs.get("uaID")));
            checkForNode(list, (long)((double)testGraphIDs.get("oaID")));
            checkForNode(list, (long)((double)testGraphIDs.get("pcID")));
        }
        catch (NodeNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void createPolicy() {
        String sessionID = getSessionID();
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        
        //create policy class
        Response response = given().
                contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"name\": \"pc1\",\"type\": \"PC\",\"properties\":{\"uuid\":\"" + uuid + "\"}}")
                .when()
                .post(URI + "/policies" + "?session=" + sessionID);
        response.then().body("code", Matchers.is(ApiResponseCodes.SUCCESS));
        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        LinkedTreeMap m = (LinkedTreeMap) res.getEntity();
        assertEquals(m.get("name"), "pc1");

        //create policy class with same name different namespace property
        response = given().
                contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"name\": \"pc1\",\"type\": \"PC\",\"properties\":{\"uuid\":\"" + uuid + "\",\"namespace\":\"pc1\"}}")
                .when()
                .post(URI + "/policies" + "?session=" + sessionID);
        response.then().body("code", Matchers.is(ApiResponseCodes.SUCCESS));
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        m = (LinkedTreeMap) res.getEntity();
        assertEquals(m.get("name"), "pc1");
        
        //create already existing policy class name same property, this should return an error
        response = given().
                contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"name\": \"pc1\",\"type\": \"PC\",\"properties\":{\"uuid\":\"" + uuid + "\",\"namespace\":\"pc1\"}}")
                .when()
                .post(URI + "/policies" + "?session=" + sessionID);
        System.out.println(response.asString());
        response.then().body("code", Matchers.is(ApiResponseCodes.ERR_NODE_NAME_EXISTS_IN_NAMESPACE));
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