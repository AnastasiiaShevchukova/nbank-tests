package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class DepositMoneyTest {

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter())
        );
    }

    //Positive:
    public static Stream<Arguments> moneyValidDepositData() {
        return Stream.of(
                Arguments.of("ira20029", "Ira2000!",1),
                Arguments.of("ira20030", "Ira2001!",2500),
                Arguments.of("ira20031", "Ira2002!",4999),
                Arguments.of("ira20032", "Ira2003!",5000)
        );
    }

    // User can deposit money 1 - 5000 rouble
    @ParameterizedTest
    @MethodSource("moneyValidDepositData")
    public void userCanDepositMoneyTest(String username, String password, Integer depositAmount){
        //создание пользователя
        String requestBody = String.format(
                """
                          {
                          "username": "%s",
                          "password": "%s",
                          "role": "USER"
                        }
                        """, username, password);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo(username))
                .body("password", Matchers.not(Matchers.equalTo(password)))
                .body("role", Matchers.equalTo("USER"));

        // получаем токен
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт и получаем его ID
        Integer accountId = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("balance", Matchers.equalTo(0.0f))
                .extract()
                .path("id");


        // депозит
        String depositBody = String.format(
                """
                {
                  "id": %d,
                  "balance": %d
                }
                """, accountId, depositAmount);

        // депозит
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(depositBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(accountId))
                .body("balance", Matchers.equalTo(depositAmount.floatValue()));
    }




    //Negative:
    public static Stream<Arguments> moneyInvalidDepositData() {
        return Stream.of(
                Arguments.of("ira20030", "Ira2000!",-1, HttpStatus.SC_BAD_REQUEST, "Deposit amount must be at least 0.01"),
                Arguments.of("ira20031", "Ira2001!",0, HttpStatus.SC_BAD_REQUEST, "Deposit amount must be at least 0.01"),
                Arguments.of("ira20032", "Ira2002!",5001, HttpStatus.SC_BAD_REQUEST, "Deposit amount cannot exceed 5000")
        );
    }

    // User can not deposit money < 0 or > 5000 rouble
    @ParameterizedTest
    @MethodSource("moneyInvalidDepositData")
    public void userCanNotDepositMoneyTest(String username, String password, Integer depositAmount, Integer error, String errorMsg){
        //создание пользователя
        String requestBody = String.format(
                """
                          {
                          "username": "%s",
                          "password": "%s",
                          "role": "USER"
                        }
                        """, username, password);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo(username))
                .body("password", Matchers.not(Matchers.equalTo(password)))
                .body("role", Matchers.equalTo("USER"));

        // получаем токен
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт и получаем его ID
        Integer accountId = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("balance", Matchers.equalTo(0.0f))
                .extract()
                .path("id");


        // депозит
        String depositBody = String.format(
                """
                {
                  "id": %d,
                  "balance": %d
                }
                """, accountId, depositAmount);

        // депозит
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(depositBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(error)
                .body(Matchers.equalTo(errorMsg));
    }








    //Negative
    // User can not deposit money into ANOTHER ACCOUNT
    @Test
    public void userCanNotDepositMoneyIntoAnotherAccountTest(){
        //создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "ira20033",
                          "password": "Ira2000!",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo("ira20033"))
                .body("password", Matchers.not(Matchers.equalTo("Ira2000!")))
                .body("role", Matchers.equalTo("USER"));

        // получаем токен
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "ira20033",
                          "password": "Ira2000!",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт и получаем его ID
        Integer accountId = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("balance", Matchers.equalTo(0.0f))
                .extract()
                .path("id");

        // депозит
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "id": 2000000,
                          "balance": 10
                        }
                        """)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.equalTo("Unauthorized access to account"));
    }
}
