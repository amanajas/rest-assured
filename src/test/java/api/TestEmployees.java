package api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.lessThan;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

@TestMethodOrder(OrderAnnotation.class)
public class TestEmployees {

    public static final String API_URL = "http://127.0.0.1:5002/employees";

    @Test
    public void testStatus() {
        given().when().get(API_URL).then().assertThat().statusCode(200);
    }
    @Test
    public void testResponseTime() { given().when().get(API_URL).then().time(lessThan(500L)); }

    @Test
    @Order(1)
    public void testListEmployees() {
        given().when().get(API_URL)
                .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("employees.json"));
    }

    @Test
    @Order(2)
    public void testCreateSuccessResponse() {
        JSONObject requestParams = new JSONObject();
        requestParams.put("first_name", "Test");
        requestParams.put("last_name", "API");
        Response response = given()
                .contentType(ContentType.TEXT)
                .baseUri(API_URL)
                .queryParams(requestParams.toMap())
                .post("/create")
                .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON).extract().response();
        Assertions.assertTrue(response.jsonPath().getString("success").contains("Employee created"));
    }

    @Test
    @Order(3)
    public void testDeleteSuccessResponse() {
        Response response = given().baseUri(API_URL).post("/delete/last")
                .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(200)
                .contentType(ContentType.JSON).extract().response();
        Assertions.assertTrue(response.jsonPath().getString("success").contains("Number of rows deleted 1"));
    }

    @Test
    public void testGetDataSchema() {
        given().when().baseUri(API_URL).get("/1")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("employeesData.json"));
    }


    @Test
    public void testDeleteSkippedResponse() {
        Response response = given().baseUri(API_URL).urlEncodingEnabled(true)
                .queryParam("employee_id", -1)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .post("/delete")
                .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(200)
                .contentType(ContentType.JSON).extract().response();
        Assertions.assertTrue(response.jsonPath().getString("skipped").contains("No employee was deleted"));
    }

    @Test
    public void testCreateFailResponse() {
        JSONObject requestParams = new JSONObject();
        requestParams.put("first_name", "TestFail");
        Response response = given()
                .contentType(ContentType.TEXT)
                .baseUri(API_URL)
                .queryParams(requestParams.toMap())
                .post("/create")
                .then()
                .statusCode(200).extract().response();
        Assertions.assertTrue(response.jsonPath().getString("error").contains("Field names are required"));
        requestParams.remove("first_name");
        requestParams.put("last_name", "TestFail");
        response = given()
                .contentType(ContentType.TEXT)
                .baseUri(API_URL)
                .queryParams(requestParams.toMap())
                .post("/create")
                .then()
                .statusCode(200).extract().response();
        Assertions.assertTrue(response.jsonPath().getString("error").contains("Field names are required"));
        requestParams.put("first_name", "TestFail");
        requestParams.put("birth_date", "13.03.2007");
        response = given()
                .contentType(ContentType.TEXT)
                .baseUri(API_URL)
                .queryParams(requestParams.toMap())
                .post("/create")
                .then()
                .statusCode(200).extract().response();
        Assertions.assertTrue(response.jsonPath().getString("error").contains("Verify your parameters"));
        requestParams.remove("birth_date");
        requestParams.put("reports_to", "something");
        response = given()
                .contentType(ContentType.TEXT)
                .baseUri(API_URL)
                .queryParams(requestParams.toMap())
                .post("/create")
                .then()
                .statusCode(200).extract().response();
        Assertions.assertTrue(response.jsonPath().getString("error").contains("Verify your parameters"));
    }

    @Test
    public void testDeleteFailResponse() {
        JSONObject requestParams = new JSONObject();
        requestParams.put("employee_id", "TestFail");
        Response response = given()
                .contentType(ContentType.TEXT)
                .baseUri(API_URL)
                .queryParams(requestParams.toMap())
                .post("/delete")
                .then()
                .statusCode(200).extract().response();
        Assertions.assertTrue(response.jsonPath().getString("error").contains("Invalid employee ID"));
    }

    @Test
    public void testGetDataFailResponse() {
        Response response = given()
                .contentType(ContentType.TEXT)
                .baseUri(API_URL)
                .get("/a1")
                .then()
                .statusCode(200).extract().response();
        Assertions.assertTrue(response.jsonPath().getString("error").contains("Invalid employee ID"));
    }

}
