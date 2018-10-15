package gov.nist.csd.pm.pep.resources;

import com.google.gson.Gson;
import gov.nist.csd.pm.pep.response.ApiResponse;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;

import static io.restassured.RestAssured.given;

public class PMTest {

    protected static final String BASE_URI = "http://localhost:8080/pm/api";

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

    public void deleteNodes(String uuid) {
        // get nodes with property uuid = uuid
        // delete each node
    }
}
