package gov.nist.csd.pm.pep.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.internal.LinkedTreeMap;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.pep.response.ApiResponse;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionResourceTest extends PMTest {

    private static final String URI = BASE_URI + "/sessions";

    @Test
    void createSession() {
        Response response = given().
                contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"username\": \"super\",\"password\": \"super\"}")
                .when()
                .post(URI);
        response.then().body("code", Matchers.is(9000));
        response.then().body("entity", Matchers.any(String.class));
    }

    @Test
    void deleteSession() {
        Response response = given().
                contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"username\": \"super\",\"password\": \"super\"}")
                .when()
                .post(URI);
        response.then().body("code", Matchers.is(9000));
        response.then().body("entity", Matchers.any(String.class));

        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        String session = (String) res.getEntity();

        response = delete(URI + "/" + session);
        response.then().body("code", Matchers.is(9000));
    }

    @Test
    void getSessionUser() {
        Response response = given().
                contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"username\": \"super\",\"password\": \"super\"}")
                .when()
                .post(URI);
        response.then().body("code", Matchers.is(9000));
        response.then().body("entity", Matchers.any(String.class));

        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        String session = (String) res.getEntity();

        response = get(URI + "/" + session);
        response.then().body("code", Matchers.is(9000));
        res = new Gson().fromJson(response.asString(), ApiResponse.class);
        LinkedTreeMap m = (LinkedTreeMap) res.getEntity();
        assertEquals(3, (double)m.get("id"));
    }
}