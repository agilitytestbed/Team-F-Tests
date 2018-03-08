/*
 * Copyright (c) 2018, Joost Prins <github.com/joostprins>, Tom Leemreize <https://github.com/oplosthee>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.utwente.ing;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static org.junit.Assert.assertEquals;

public class CategoryTests {

    private static final Path CATEGORY_LIST_SCHEMA_PATH = Paths.get("src/test/java/nl/utwente/ing/schemas/categories/category-list.json");
    private static final Path CATEGORY_SCHEMA_PATH = Paths.get("src/test/java/nl/utwente/ing/schemas/categories/category-list.json");

    private static final int TEST_CATEGORY_ID = 66828978;
    private static final String TEST_CATEGORY_NAME = "Test Category";

    private static Integer sessionId;

    /**
     * Makes sure all tests share the same session ID by setting sessionId if does not exist yet.
     */
    @Before
    public void getTestSession() {
        if (sessionId == null) {
            String response = given()
                    .get("api/v1/categories")
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .extract()
                    .response()
                    .asString();

            sessionId = Integer.parseInt(response);
        }
    }

    /**
     * Deletes the category used for testing before running every test to avoid errors due to duplicate entries.
     */
    @Before
    public void deleteTestCategory() {
        given()
                .header("WWW_Authenticate", sessionId)
                .body(String.format("{\"id\": %d, \"name\": \"%s\"}", TEST_CATEGORY_ID, TEST_CATEGORY_NAME))
                .delete(String.format("api/v1/categories/%d", TEST_CATEGORY_ID))
                .then()
                .assertThat()
                .statusCode(204);
    }

    /*
     *  Tests related to GET requests on the /categories API endpoint.
     *  API Documentation: https://app.swaggerhub.com/apis/djhuistra/INGHonours/1.0.1#/categories/get_categories
     */

    /**
     * Performs a GET request on the categories endpoint.
     *
     * This test uses a valid session ID and tests whether the output is formatted according to the specification.
     */
    @Test
    public void validSessionCategoriesGetTest() {
        given()
                .header("WWW_Authenticate", sessionId)
                .get("api/v1/categories")
                .then()
                .assertThat()
                .body(matchesJsonSchema(CATEGORY_LIST_SCHEMA_PATH.toAbsolutePath().toUri()))
                .statusCode(200);
    }

    /**
     * Performs a GET request on the categories endpoint.
     *
     * This test uses an invalid session ID and checks whether the resulting status code is 401 Unauthorized.
     */
    @Test
    public void invalidSessionCategoriesGetTest() {
        get("api/v1/categories")
                .then()
                .assertThat()
                .statusCode(401);
    }

    /*
     *  Tests related to POST requests on the /categories API endpoint.
     *  API Documentation: https://app.swaggerhub.com/apis/djhuistra/INGHonours/1.0.1#/categories/post_categories
     */

    /**
     * Performs a POST request on the categories endpoint.
     *
     * This test uses a valid session ID and a body formatted according to the given specification for a Transaction.
     * This test will check whether the resulting status code is 201 Created.
     */
    @Test
    public void categoriesPostTest() {
        given()
                .header("WWW_Authenticate", sessionId)
                .body(String.format("{\"id\": %d, \"name\": \"%s\"}", TEST_CATEGORY_ID, TEST_CATEGORY_NAME))
                .post("api/v1/categories")
                .then()
                .assertThat()
                .body(matchesJsonSchema(CATEGORY_SCHEMA_PATH.toAbsolutePath().toUri()))
                .statusCode(201);
    }

    /**
     * Performs a POST request on the categories endpoint.
     *
     * This test uses an invalid session ID and a body formatted according to the given specification for a Transaction.
     * This test will check whether the resulting status code is 401 Unauthorized.
     */
    @Test
    public void invalidSessionCategoriesPostTest() {
        given()
                .body(String.format("{\"id\": %d, \"name\": \"%s\"}", TEST_CATEGORY_ID, TEST_CATEGORY_NAME))
                .post("api/v1/categories")
                .then()
                .assertThat()
                .statusCode(401);
    }

    /**
     * Performs a POST request on the categories endpoint.
     *
     * This test uses a valid session ID and an invalid body.
     * This test will check whether the resulting status code is 405 Method Not Allowed.
     */
    @Test
    public void invalidFormatCategoriesPostTest() {
        given()
                .header("WWW_Authenticate", sessionId)
                .body(String.format("{\"invalid\": %d, \"name\": \"%s\"}", TEST_CATEGORY_ID, TEST_CATEGORY_NAME))
                .post("api/v1/categories")
                .then()
                .assertThat()
                .statusCode(405);
    }

    /*
     *  Tests related to GET requests on the /categories/{categoryId} API endpoint.
     *  API Documentation: https://app.swaggerhub.com/apis/djhuistra/INGHonours/1.0.1#/categories/get_categories__categoryId_
     */

    /**
     * Performs a GET request on the categories/{categoryId} endpoint.
     *
     * This test uses a valid session ID and tests whether a previously created category can be fetched and is formatted
     * according to the specification.
     */
    @Test
    public void validSessionByIdGetTest() {
        // Use the /categories POST test to create the test category.
        categoriesPostTest();

        String categoryName = given()
                .header("WWW_Authenticate", sessionId)
                .get(String.format("api/v1/categories/%d", TEST_CATEGORY_ID))
                .then()
                .assertThat()
                .body(matchesJsonSchema(CATEGORY_SCHEMA_PATH.toAbsolutePath().toUri()))
                .statusCode(200)
                .extract()
                .response()
                .getBody()
                .jsonPath()
                .getString("name");

        assertEquals(categoryName, TEST_CATEGORY_NAME);
    }

}
