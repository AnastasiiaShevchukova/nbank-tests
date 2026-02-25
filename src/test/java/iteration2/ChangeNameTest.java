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

public class ChangeNameTest {

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter())
        );
    }

    // Positive:
    @Test
    public void userCanChangeNameTest() {
        //создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "olga2002",
                          "password": "Olga2001!",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo("olga2002"))
                .body("password", Matchers.not(Matchers.equalTo("Olga2001!")))
                .body("role", Matchers.equalTo("USER"));

        // получаем токен
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                                  "username": "olga2002",
                                  "password": "Olga2001!" 
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeader)
                .body("""
                        {
                          "name": "John Smith"
                        }
                        """)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("message", Matchers.equalTo("Profile updated successfully"))
                .body("customer.username", Matchers.equalTo("olga2002"))
                .body("customer.name", Matchers.equalTo("John Smith"));
    }


    //Negative:
    public static Stream<Arguments> invalidNameData() {
        return Stream.of(
                Arguments.of("olga20002", "Ira2000!", "John", "Name must contain two words with letters only"),
                Arguments.of("olga20003", "Ira2001!", "John John John", "Name must contain two words with letters only"),
                Arguments.of("olga20004", "Ira2002!", "123 123", "Name must contain two words with letters only"),
                Arguments.of("olga20005", "Ira2002!", "^$# **& ^$# **&", "Name must contain two words with letters only"),
                Arguments.of("olga20006", "Ira2002!", "", "Name must contain two words with letters only"),
                Arguments.of("olga20007", "Ira2002!", "    ", "Name must contain two words with letters only")
        );
    }

    // User can NOT change name
    @ParameterizedTest
    @MethodSource("invalidNameData")
    public void userCanNotChangeNameTest(String username, String password, String newNameValue, String errorMsg) {
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

        // меняем имя пользователя
        String customerNameBody = String.format(
                """
                        {
                                  "name": "%s"
                                }
                        """, newNameValue);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeader)
                .body(customerNameBody)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorMsg));

    }
}
