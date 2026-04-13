package apiTests.iteration1;

import apiTests.BaseTest;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.GetAllUserResponse;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

public class CreateUserTest extends BaseTest {


    // Positive:
    @Test
    public void adminCanCreateUserWithCorrectDataTest() {
        CreateUserRequest createUserRequest = RandomModelGenerator.generate(CreateUserRequest.class);
        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>
                (RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated())
                .post(createUserRequest);
        ModelAssertions.assertThatModels(createUserRequest, createUserResponse).match();

        // запросить все созданные админом аккаунты и проверить, что созданный юзер там
        List<GetAllUserResponse> allUsers = AdminSteps.gelAllUsers();

        // Находим созданного пользователя в списке
        GetAllUserResponse createdUserInList = allUsers.stream()
                .filter(user -> user.getUsername().equals(createUserRequest.getUsername()))
                .findFirst()
                .orElse(null);

        softly.assertThat(createdUserInList).isNotNull();
        softly.assertThat(createdUserInList.getUsername()).isEqualTo(createUserRequest.getUsername());
        softly.assertThat(createdUserInList.getRole()).isEqualTo(createUserRequest.getRole());
    }


    //Negative:
    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                Arguments.of("    ", "Password22$", "USER", "username", "Username cannot be blank"),
                Arguments.of("ab", "Password22$", "USER", "username", "Username must be between 3 and 15 characters"),
                Arguments.of("abc$", "Password22$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("abc%", "Password22$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots")
        );
    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidDataTest(String username, String password, String role, String errorKey, String errorValue) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        new CrudRequester(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue))
                .post(createUserRequest);
    }
}
