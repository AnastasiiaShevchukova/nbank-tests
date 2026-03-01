package apiTests.iteration1;

import apiTests.BaseTest;
import generators.RandomData;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.GetAllUserResponse;
import models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.GetAllUsersRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

public class CreateUserTest extends BaseTest {


    // Positive:
    @Test
    public void adminCanCreateUserWithCorrectDataTest() {
        //создание пользователя
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        CreateUserResponse createUserResponse = new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUserRequest)
                .extract().as(CreateUserResponse.class);
        softly.assertThat(createUserRequest.getUsername()).isEqualTo(createUserResponse.getUsername());
        softly.assertThat(createUserRequest.getPassword()).isNotEqualTo(createUserResponse.getPassword());
        softly.assertThat(createUserRequest.getRole()).isEqualTo(createUserResponse.getRole());

        // запросить все созданные админом аккаунты и проверить, что созданный юзер там
        List<GetAllUserResponse> allUsers = new GetAllUsersRequester(RequestSpecs.adminSpec(), ResponseSpecs.requestReturnsOK())
                .post(null)
                .extract().jsonPath().getList("",GetAllUserResponse.class);

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

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue))
                .post(createUserRequest);
    }
}
