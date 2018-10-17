package gov.nist.csd.pm.pep.resources;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pep.response.ApiResponseCodes;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;

import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PMTest {

    static final String BASE_URI = "http://localhost:8080/pm/api";
    private static final String TEST_URI  = BASE_URI + "/tests/graph";

    public String getSessionID() {
        Response response = given().
                contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"username\": \"super\",\"password\": \"super\"}")
                .when()
                .post("http://localhost:8080/pm/api/sessions");
        response.then().body("code", Matchers.is(9000));
        response.then().body("entity", Matchers.any(String.class));

        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        return (String) res.getEntity();
    }

    public void deleteSession(String sessionID) {
        delete(BASE_URI + "/sessions/" + sessionID);
    }

    String sessionID;
    String uuid;

    @BeforeEach
    void init() {
        sessionID = getSessionID();
        uuid = UUID.randomUUID().toString().replaceAll("-", "");
    }

    @AfterEach
    void tearDown() {
        Response response = given().delete(TEST_URI + "/" + uuid);
        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ApiResponseCodes.SUCCESS, res.getCode(),
                String.format("expected a success response code (9000) but received %s", res.getCode()));

        deleteSession(sessionID);
    }

    LinkedTreeMap buildTestGraph() {
        Response response = given().queryParam("session", sessionID).get(TEST_URI);
        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        assertEquals(ApiResponseCodes.SUCCESS, res.getCode(),
                String.format("expected a success response code (9000) but received %s", res.getCode()));

        LinkedTreeMap testGraph = (LinkedTreeMap) res.getEntity();
        uuid = (String) testGraph.get("uuid");

        return testGraph;
    }

    long idToLong(Object id) {
        return (long)((double)id);
    }
}
