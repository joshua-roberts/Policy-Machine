/*
package gov.nist.csd.pm.pep.resources;

import com.google.gson.Gson;
import gov.nist.csd.pm.pep.response.ApiResponse;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;

import java.util.UUID;

import static io.restassured.RestAssured.given;

public class TestGraph {

    private String uuid;

    public TestGraph() {
        uuid = UUID.randomUUID().toString();
    }

    public void build() {
        Response response = given().
                contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"name\": \"pc1\",\"type\": \"PC\", \"properties\": {\"uuid\": \"" + uuid + "\"}}")
                .when()
                .post("http://localhost:8080/pm/api/nodes/policies");
        System.out.println(response.asString());
        ApiResponse res = new Gson().fromJson(response.asString(), ApiResponse.class);
        System.out.println(res.getEntity());
    }
}
*/
