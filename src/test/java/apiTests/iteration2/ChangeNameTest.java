package apiTests.iteration2;

import apiTests.BaseTest;
import generators.RandomData;
import models.ChangeNameRequest;
import models.ChangeNameResponse;
import models.CreateUserRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.ChangeNameRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class ChangeNameTest extends BaseTest {

    // Positive 1:
    @Test
    public void userCanChangeNameTest() {
        //создание пользователя
        String username = RandomData.getUserName();
        String password = RandomData.getUserPassword();

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name("John Smith")
                .build();

        ChangeNameResponse changeNameResponse = new ChangeNameRequester(RequestSpecs.authAsUserSpec(username, password), ResponseSpecs.requestReturnsOK())
                .put(changeNameRequest)
                .extract().as(ChangeNameResponse.class);

        softly.assertThat(changeNameResponse.getMessage()).isEqualTo("Profile updated successfully");
        softly.assertThat(changeNameResponse.getCustomer().getUsername()).isEqualTo(username);
        softly.assertThat(changeNameResponse.getCustomer().getName()).isEqualTo("John Smith");
    }


    //Negative 1:
    public static Stream<Arguments> invalidNameData() {
        return Stream.of(
                Arguments.of("John", "Name must contain two words with letters only"),
                Arguments.of("John John John", "Name must contain two words with letters only"),
                Arguments.of("123 123", "Name must contain two words with letters only"),
                Arguments.of("^$# **& ^$# **&", "Name must contain two words with letters only"),
                Arguments.of("", "Name must contain two words with letters only"),
                Arguments.of("    ", "Name must contain two words with letters only")
        );
    }

    // User can NOT change name
    @ParameterizedTest
    @MethodSource("invalidNameData")
    public void userCanNotChangeNameTest(String newNameValue, String errorMsg) {
        //создание пользователя
        String username = RandomData.getUserName();
        String password = RandomData.getUserPassword();

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(newNameValue)
                .build();

        new ChangeNameRequester(RequestSpecs.authAsUserSpec(username, password), ResponseSpecs.requestReturnsBadRequestWithoutErrorKey(errorMsg))
                .put(changeNameRequest);

    }
}
