package apiTests.iteration2;

import api.generators.RandomData;
import api.requests.steps.DataBaseSteps;
import apiTests.BaseTest;
import api.models.ChangeNameRequest;
import api.models.ChangeNameResponse;
import api.models.CreateUserRequest;
import common.annotations.APIBackend;
import common.annotations.APIVersion;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@APIVersion(APIBackend.DATABASE_FIX)
public class ChangeNameTest extends BaseTest {

    // Positive 1:
    @Test
    @DisplayName("User can change name")
    public void userCanChangeNameTest() {
        CreateUserRequest user= AdminSteps.createUser();
        String newUsername = RandomData.getRandomValidUsername();

        ChangeNameResponse changeNameResponse = UserSteps.changeName(user, newUsername);

        // проверка, что имя поменялось
        // проверка через АПИ, что имя поменялось
        UserSteps.checkName(user, newUsername, "Ожидалось, что имя юзера изменится");

        softly.assertThat(changeNameResponse.getMessage()).isEqualTo("Profile updated successfully");
        softly.assertThat(changeNameResponse.getCustomer().getUsername()).isEqualTo(user.getUsername());
        softly.assertThat(changeNameResponse.getCustomer().getName()).isEqualTo(newUsername);

        // Проверка через БД, что имя поменялось
        //String expectedName = changeNameResponse.getCustomer().getName();
        //String actualName = DataBaseSteps.getUserByUsername(user.getUsername()).getName();
        //assertEquals(expectedName, actualName, "Ожидалось, что имя юзера в БД изменится");

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
        CreateUserRequest user = AdminSteps.createUser();

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(newNameValue)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsBadRequestWithoutErrorKey(errorMsg))
                .update(changeNameRequest);

        // проверка через АПИ, что имя не поменялось
        UserSteps.checkName(user, null, "Ожидалось, что имя юзера не изменится");

        // Проверка через БД, что имя не поменялось
        //String actualName = DataBaseSteps.getUserByUsername(user.getUsername()).getName();
        //assertEquals(null, actualName, "Ожидалось, что имя юзера в БД не изменится");
    }
}
