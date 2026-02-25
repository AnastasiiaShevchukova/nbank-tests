package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class TransferMoneyTest {

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter())
        );
    }


    //Positive:
    public static Stream<Arguments> moneyValidTransferData() {
        return Stream.of(
                Arguments.of("ira3004", "Ira2000!",5000, 1, 1),
                Arguments.of("ira3005", "Ira2001!",5000, 3000, 1),
                Arguments.of("ira3007", "Ira2002!",5000, 9999, 2),
                Arguments.of("ira3008", "Ira2002!",5000, 10000, 2)
        );
    }

    // User can transfer money 1 - 10 000 rouble
    @ParameterizedTest
    @MethodSource("moneyValidTransferData")
    public void userCanDepositMoneyTest(String username, String password, Integer depositAmount, Integer transferAmount, Integer depositCount){
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

        // создаем первый аккаунт и получаем его ID
        Integer firstAccountId = given()
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


        // Выполняем депозит  денег на первый аккаунт указанное количество раз (5000 или 10 000 или 15 000 и т.д)
        int totalDeposited = 0;
        for (int i = 0; i < depositCount; i++) {
            String depositBody = String.format(
                    """
                    {
                      "id": %d,
                      "balance": %d
                    }
                    """, firstAccountId, depositAmount);

            // депозит (максимум 5000 за раз)
            given()
                    .header("Authorization", userAuthHeader)
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body(depositBody)
                    .post("http://localhost:4111/api/v1/accounts/deposit")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("id", Matchers.equalTo(firstAccountId));

            totalDeposited += depositAmount;
        }

        // создаем второй аккаунт и получаем его ID
        Integer secondAccountId = given()
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


        // Перевод денег между аккаунтами
        String transferBody = String.format(
                """
                {
                  "senderAccountId": %d,
                  "receiverAccountId": %d,
                  "amount": %d
                }
                """, firstAccountId, secondAccountId, transferAmount);

        // перевод денег (максимум 10 000 за 1 итерацию)
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(transferBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("message", Matchers.equalTo("Transfer successful"))
                .body("amount", Matchers.equalTo(transferAmount.floatValue()))
                .body("receiverAccountId", Matchers.equalTo(secondAccountId))
                .body("senderAccountId", Matchers.equalTo(firstAccountId));
    }


















    //Negative:
    public static Stream<Arguments> moneyInvalidTransferData() {
        return Stream.of(
                Arguments.of("ira4001", "Ira2000!",5000, -10, 1, "Transfer amount must be at least 0.01"),
                Arguments.of("ira4002", "Ira2001!",5000, 0, 1, "Transfer amount must be at least 0.01"),
                Arguments.of("ira4003", "Ira2002!",5000, 10001, 2, "Transfer amount cannot exceed 10000"),
                Arguments.of("ira4004", "Ira2002!",5000, 5001, 1, "Invalid transfer: insufficient funds or invalid accounts")
        );
    }

    // User can NOT transfer money
    @ParameterizedTest
    @MethodSource("moneyInvalidTransferData")
    public void userCanNotDepositMoneyTest(String username, String password, Integer depositAmount, Integer transferAmount, Integer depositCount, String errorMsg){
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

        // создаем первый аккаунт и получаем его ID
        Integer firstAccountId = given()
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


        // Выполняем депозит  денег на первый аккаунт указанное количество раз (5000 или 10 000 или 15 000 и т.д)
        int totalDeposited = 0;
        for (int i = 0; i < depositCount; i++) {
            String depositBody = String.format(
                    """
                    {
                      "id": %d,
                      "balance": %d
                    }
                    """, firstAccountId, depositAmount);

            // депозит (максимум 5000 за раз)
            given()
                    .header("Authorization", userAuthHeader)
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body(depositBody)
                    .post("http://localhost:4111/api/v1/accounts/deposit")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("id", Matchers.equalTo(firstAccountId));

            totalDeposited += depositAmount;
        }

        // создаем второй аккаунт и получаем его ID
        Integer secondAccountId = given()
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


        // Перевод денег между аккаунтами
        String transferBody = String.format(
                """
                {
                  "senderAccountId": %d,
                  "receiverAccountId": %d,
                  "amount": %d
                }
                """, firstAccountId, secondAccountId, transferAmount);

        // перевод денег (максимум 10 000 за 1 итерацию)
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(transferBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorMsg));
    }
}
