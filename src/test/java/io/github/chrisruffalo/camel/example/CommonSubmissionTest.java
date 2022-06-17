package io.github.chrisruffalo.camel.example;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * Shared methods for test cases
 */
public abstract class CommonSubmissionTest {

    /**
     * Given a response extracts the value of the conversation id from
     * the "thanks for" message.
     *
     * @param response the response to extract the conversation id from
     * @return the response
     */
    protected String extractConversationIdFromResponse(final Response response) {
        return response.body().asString().substring("thanks for: ".length());
    }

    /**
     * Put the given string to the submission end point
     *
     * @param data a string to post
     * @return the Response from the Rest Assured API
     */
    protected Response putSubmissionData(final String data) {
       return RestAssured.given().contentType("application/json")
                .body(data)
                .put("/submission")
                .then()
                .statusCode(200)
                .extract().response();
    }

    /**
     * Post the given string to the submission end point
     *
     * @param data a string to post
     * @return the Response from the Rest Assured API
     */
    protected Response postSubmissionData(final String data) {
        return RestAssured.given().contentType("application/json")
                .body(data)
                .post("/submission")
                .then()
                .statusCode(200)
                .extract().response();
    }

}
