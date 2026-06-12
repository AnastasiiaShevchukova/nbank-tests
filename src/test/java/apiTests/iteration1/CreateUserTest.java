package apiTests.iteration1;

import api.dao.UserDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.requests.steps.DataBaseSteps;
import apiTests.BaseTest;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.GetAllUserResponse;
import api.models.comparison.ModelAssertions;
import common.annotations.APIBackend;
import common.annotations.APIVersion;
import common.annotations.AdminSession;
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

import static org.junit.jupiter.api.Assertions.assertNull;

@APIVersion(APIBackend.DATABASE_FIX)
public class CreateUserTest extends BaseTest {


    // Positive:
    @Test
    public void adminCanCreateUserWithCorrectDataTest() {
        // Подготовка данных
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

        // POST запрос
        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>
                (RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated())
                .post(newUser);
        // Проверка изначально созданного DTO с полученным DTO
        ModelAssertions.assertThatModels(newUser, createUserResponse).match();

        // GET запрос для проверки созданного юзера
        // Запросить все созданные админом аккаунты и проверить, что созданный юзер там
        List<GetAllUserResponse> allUsers = AdminSteps.gelAllUsers();

        // Находим созданного пользователя в списке
        GetAllUserResponse createdUserInList = allUsers.stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst()
                .orElse(null);

        softly.assertThat(createdUserInList).isNotNull();
        softly.assertThat(createdUserInList.getUsername()).isEqualTo(newUser.getUsername());
        softly.assertThat(createdUserInList.getRole()).isEqualTo(newUser.getRole());

        // Проверка через базу данных
        //UserDao userDao = DataBaseSteps.getUserByUsername(newUser.getUsername());
        //DaoAndModelAssertions.assertThat(createUserResponse, userDao).match();
    }


    //Negative:
    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                Arguments.of("    ", "Password22$", "USER", "username", List.of("Username cannot be blank", "Username must contain only letters, digits, dashes, underscores, and dots")),
                Arguments.of("ab", "Password22$", "USER", "username", List.of("Username must be between 3 and 15 characters")),
                Arguments.of("abc$", "Password22$", "USER", "username", List.of("Username must contain only letters, digits, dashes, underscores, and dots")),
                Arguments.of("abc%", "Password22$", "USER", "username", List.of("Username must contain only letters, digits, dashes, underscores, and dots"))
        );
    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidDataTest(String username, String password, String role, String errorKey, List<String> errorValues) {
        CreateUserRequest newUser = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        new CrudRequester(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.requestReturnsBadRequest(errorKey, errorValues))
                .post(newUser);

        // Проверка через БД
        //assertNull(DataBaseSteps.getUserByUsername(newUser.getUsername()));
    }
}
