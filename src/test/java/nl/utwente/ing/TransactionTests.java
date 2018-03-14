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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;

public class TransactionTests {

    private static final int TEST_TRANSACTION_ID = 68796973;

    private static Integer sessionId;

    /**
     * Makes sure all tests share the same session ID by setting sessionId if does not exist yet.
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
                .delete(String.format("api/v1/transactions/%d", TEST_TRANSACTION_ID))
                .then()
                .assertThat()
                .statusCode(204);
    }

    /*
     *  Tests related to GET requests on the /transactions API endpoint.
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
                .body(String.format("{\"id\": %d, " +
                        "\"date\": \"1889-04-20T19:45:04.030Z\", " +
                        "\"amount\": 0, " +
                        "\"external-iban\": \"string\", " +
                        "\"type\": \"deposit\", " +
                        "\"category\": {\"id\": 0, \"name\": \"string\"}}",
                        TEST_TRANSACTION_ID)
                )
                .post("api/v1/categories")
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
                .body(String.format("{\"id\": %d, " +
                                "\"date\": \"1889-04-20T19:45:04.030Z\", " +
                                "\"amount\": 0, " +
                                "\"external-iban\": \"string\", " +
                                "\"type\": \"deposit\", " +
                                "\"category\": {\"id\": 0, \"name\": \"string\"}}",
                        TEST_TRANSACTION_ID)
                )
                .post("api/v1/categories")
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
                .body(String.format("{\"id\": %d, " +
                                "\"data\": \"1889-04-20T19:45:04.030Z\", " +
                                "\"category\": {\"id\": 0, \"name\": \"string\"}}",
                        TEST_TRANSACTION_ID)
                )
                .post("api/v1/categories")
                .then()
                .assertThat()
                .statusCode(405);
    }

}
