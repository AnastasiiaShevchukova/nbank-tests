package apiTests.iteration2;

import apiTests.BaseTest;
import models.ChangeNameRequest;
import models.ChangeNameResponse;
import models.CreateUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class ChangeNameTest extends BaseTest {

    // Positive 1:
    @Test
    @DisplayName("User can change name")
    public void userCanChangeNameTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        ChangeNameResponse changeNameResponse = UserSteps.changeName(userRequest, "John Smith");

        // проверка, что имя поменялось
        softly.assertThat(changeNameResponse.getMessage()).isEqualTo("Profile updated successfully");
        softly.assertThat(changeNameResponse.getCustomer().getUsername()).isEqualTo(userRequest.getUsername());
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
    @ParameterizedTest(name = "User can NOT change name")
    @MethodSource("invalidNameData")
    public void userCanNotChangeNameTest(String newNameValue, String errorMsg) {
        //создание пользователя
        CreateUserRequest userRequest = AdminSteps.createUser();

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(newNameValue)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsBadRequestWithoutErrorKey(errorMsg))
                .update(changeNameRequest);

        // проверка, что имя не поменялось
        UserSteps.checkName(userRequest, null, "Имя изменилось, хотя не должно было");
    }
}
