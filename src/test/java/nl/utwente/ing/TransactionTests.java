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

import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TransactionTests {

    private static final Path TRANSACTION_LIST_SCHEMA_PATH = Paths.get("src/test/java/nl/utwente/ing/schemas" +
                    "/transactions/transaction-list.json");
    private static final Path TRANSACTION_SCHEMA_PATH = Paths.get("src/test/java/nl/utwente/ing/schemas" +
                    "/transactions/transaction.json");

    private static final int TEST_TRANSACTION_ID_1 = 68796973;
    private static final int TEST_TRANSACTION_ID_2 = 23890471;
    private static final String TEST_TRANSACTION_1 = String.format("{\"id\": %d, " +
                    "\"date\": \"1889-04-20T19:45:04.030Z\", " +
                    "\"amount\": 0, " +
                    "\"external-iban\": \"string\", " +
                    "\"type\": \"deposit\", " +
                    "\"category\": {\"id\": 0, \"name\": \"string\"}}",
                    TEST_TRANSACTION_ID_1);
    private static final String TEST_TRANSACTION_2 = String.format("{\"id\": %d, " +
                    "\"date\": \"1889-04-20T19:45:04.030Z\", " +
                    "\"amount\": 10, " +
                    "\"external-iban\": \"strings\", " +
                    "\"type\": \"withdrawal\", " +
                    "\"category\": {\"id\": 0, \"name\": \"string\"}}",
            TEST_TRANSACTION_ID_2);
    private static final String TEST_TRANSACTION_INVALID = String.format("{\"id\": %d, " +
                    "\"data\": \"1889-04-20T19:45:04.030Z\", " +
                    "\"category\": {\"id\": 0, \"name\": \"string\"}}",
            TEST_TRANSACTION_ID_1);

    private static Integer sessionId;

    /**
     * Makes sure all tests share the same session ID by setting sessionId if it does not exist yet.
     */
    @Before
    public void getTestSession() {
        if (sessionId == null) {
            sessionId = Util.getSessionID();
        }
    }

    /**
     * Deletes the transaction used for testing before and after running every test.
     * This avoids duplicate entries and leftover entries in the database after running tests.
     */
    @Before
    @After
    public void deleteTestTransaction() {
        if (sessionId == null) {
            getTestSession();
        }

        given()
                .header("X-session-ID", sessionId)
                .delete(String.format("api/v1/transactions/%d", TEST_TRANSACTION_ID_1))
                .then()
                .assertThat()
                .statusCode(204);

        given()
                .header("X-session-ID", sessionId)
                .delete(String.format("api/v1/transactions/%d", TEST_TRANSACTION_ID_2))
                .then()
                .assertThat()
                .statusCode(204);
    }

    //TODO: Create test cases for GET /transactions offset, limit, category
    /*
     *  Tests related to GET requests on the /transactions API endpoint.
     *  API Documentation: https://app.swaggerhub.com/apis/djhuistra/INGHonours/1.0.1#/transactions
     */

    /**
     * Performs a GET request on the transactions endpoint.
     *
     * This test uses a valid session ID and tests whether the response is formatted according to the specification.
     */
    @Test
    public void validSessionTransactionsGetTest() {
        int size = given()
                .header("X-session-ID", sessionId)
                .get("api/v1/transactions")
                .then()
                .assertThat()
                .body(matchesJsonSchema(TRANSACTION_LIST_SCHEMA_PATH.toAbsolutePath().toUri()))
                .contentType(ContentType.JSON)
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("$")
                .size();

        assertThat(size, lessThanOrEqualTo(20));
    }

    /**
     * Performs a GET request on the transactions endpoint.
     *
     * This test uses an invalid session ID and checks whether the resulting status code is 401 Unauthorized.
     */
    @Test
    public void invalidSessionTransactionsGetTest() {
        get("api/v1/transactions")
                .then()
                .assertThat()
                .statusCode(401);
    }

    /*
     *  Tests related to GET requests on the /transactions/{transactionId} API endpoint.
     *  API Documentation: https://app.swaggerhub.com/apis/djhuistra/INGHonours/1.0.1#/transactions/get_transactions__transactionId_
     */

    /**
     * Performs a GET request on the transactions/{transactionId} endpoint.
     *
     * This test uses a valid session ID and tests whether a previously created transaction can be fetched and is 
     * formatted according to the specification.
     */
    @Test
    public void validSessionByIdGetTest() {
        // Use the /transactions POST test to create the test category.
        validSessionValidTransactionPostTest();

        String transaction = given()
                .header("X-session-ID", sessionId)
                .get(String.format("api/v1/transactions/%d", TEST_TRANSACTION_ID_1))
                .then()
                .assertThat()
                .body(matchesJsonSchema(TRANSACTION_SCHEMA_PATH.toAbsolutePath().toUri()))
                .contentType(ContentType.JSON)
                .statusCode(200)
                .extract()
                .response()
                .asString();

        assertEquals(transaction, TEST_TRANSACTION_1);
    }

    /*
     *  Tests related to POST requests on the /transactions API endpoint.
     *  API Documentation: https://app.swaggerhub.com/apis/djhuistra/INGHonours/1.0.1#/transactions/getTransactions
     */

    /**
     * Performs a POST request on the transactions endpoint.
     *
     * This test uses a valid session ID and tests whether it is possible to create a new transaction.
     */
    @Test
    public void validSessionValidTransactionPostTest() {
        given()
                .header("X-session-ID", sessionId)
                .body(TEST_TRANSACTION_1)
                .post("api/v1/transactions")
                .then()
                .assertThat()
                .statusCode(201);
    }

    /**
     * Performs a POST request on the transactions endpoint.
     *
     * This test uses a invalid session ID and tests whether the status code is 401 Unauthorized.
     */
    @Test
    public void invalidSessionValidCategoriesPostTest() {
        given()
                .body(TEST_TRANSACTION_1)
                .post("api/v1/transactions")
                .then()
                .assertThat()
                .statusCode(401);
    }

    /**
     * Performs a POST request on the transactions endpoint.
     *
     * This test uses a valid session ID and tests whether it is possible to create a new invalid transaction.
     */
    @Test
    public void validSessionInvalidTransactionPostTest() {
        given()
                .header("X-session-ID", sessionId)
                .body(TEST_TRANSACTION_INVALID)
                .post("api/v1/transactions")
                .then()
                .assertThat()
                .statusCode(405);
    }
}
