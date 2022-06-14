package io.github.chrisruffalo.camel.example;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * Shared methods for test cases
 */
public abstract class CommonTest {

    public Response putData(final String data) throws InterruptedException {
       return RestAssured.given().contentType("application/json")
                .body(data)
                .put("/submission")
                .then()
                .statusCode(200)
                .extract().response();
    }

    public Response postData(final String data) throws InterruptedException {
        return RestAssured.given().contentType("application/json")
                .body(data)
                .post("/submission")
                .then()
                .statusCode(200)
                .extract().response();
    }

}
