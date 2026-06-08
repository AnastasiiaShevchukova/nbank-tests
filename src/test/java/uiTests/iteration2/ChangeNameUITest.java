package uiTests.iteration2;

import api.generators.RandomData;
import api.requests.steps.DataBaseSteps;
import com.codeborne.selenide.Selenide;
import api.models.CreateUserRequest;
import common.annotations.APIBackend;
import common.annotations.APIVersion;
import common.annotations.Browsers;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.steps.UserSteps;
import ui.pages.BankAlerts;
import ui.pages.EditProfile;
import uiTests.BaseUiTest;


import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

@APIVersion(APIBackend.DATABASE_FIX)
public class ChangeNameUITest extends BaseUiTest {


    // Positive 1:
    @Test
    @DisplayName("User can change name")
    @UserSession()
    @Browsers({"firefox"})
    public void userCanChangeNameTest() {
        // Предусловие ШАГ 1: админ логинится в банке
        // Предусловие Шаг 2: админ создает юзера
        // Предусловие Шаг 3: юзер логинится в банке
        CreateUserRequest user = SessionStorage.getUser();
        // ШАГИ ТЕСТА:
        // Тест Шаг 1: Открыть меню изменения имени юзера
        // Тест Шаг 2: Ввести новое имя пользователя
        // Тест Шаг 3: Нажать кнопку "💾 Save Changes"
        // Проверка UI, что имя пользователя обновилось успешно
        String newUsername = RandomData.getRandomValidUsername();
        new EditProfile().open().updateUserName(newUsername)
                .checkAlertMessageAndAccept(BankAlerts.NAME_UPDATED_SUCCESSFULLY.getMessage());

        Selenide.open("/dashboard");
        $(".user-name").shouldHave(text(newUsername)).shouldBe(visible);

        // Проверка API, что имя поменялось
        UserSteps.checkName(user, newUsername, "Ожидалось, что имя пользователя изменится на новое значение");

        // Проверка через БД, что имя поменялось
        //String actualName = DataBaseSteps.getUserByUsername(user.getUsername()).getName();
        //assertEquals(newUsername, actualName, "Ожидалось, что имя юзера в БД изменится");
    }

    private static Stream<Arguments> invalidNameDataProvider() {
        return Stream.of(
                Arguments.of("John", BankAlerts.PLEASE_ENTER_A_VALID_NAME.getMessage()),
                Arguments.of("John John John", BankAlerts.PLEASE_ENTER_A_VALID_NAME.getMessage()),
                Arguments.of("", BankAlerts.PLEASE_ENTER_A_VALID_NAME.getMessage()),
                Arguments.of("     ", BankAlerts.PLEASE_ENTER_A_VALID_NAME.getMessage()),
                Arguments.of("123 123", BankAlerts.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getMessage()),
                Arguments.of("^$# **& ^$# **&", BankAlerts.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getMessage())
        );
    }

    //Negative 1:
    @ParameterizedTest(name = "User can NOT change name {0}")
    @MethodSource("invalidNameDataProvider")
    @UserSession()
    @Browsers({"chrome"})
    public void userCanNotChangeNameTest(String newNameValue, String expectedAlertMessage) {
        // Предусловие ШАГ 1: админ логинится в банке
        // Предусловие Шаг 2: админ создает юзера
        // Предусловие Шаг 3: юзер логинится в банке
        CreateUserRequest user = SessionStorage.getUser();
        // ШАГИ ТЕСТА:
        // Тест Шаг 1: Открыть меню изменения имени юзера
        // Тест Шаг 2: Ввести новое имя пользователя
        // Тест Шаг 3: Нажать кнопку "💾 Save Changes"
        // Проверка UI, что имя пользователя НЕ обновилось успешно
        new EditProfile().open().updateUserName(newNameValue)
                .checkAlertMessageAndAccept(expectedAlertMessage);

        Selenide.open("/dashboard");
        $(".user-name").shouldHave(text("Noname")).shouldBe(visible);

        // Проверка API, что имя юзера не поменялось
        UserSteps.checkName(user, null, "Ожидалось, что имя юзера не поменяется");

        // Проверка через БД, что имя не поменялось
        //String actualName = DataBaseSteps.getUserByUsername(user.getUsername()).getName();
        //assertEquals(null, actualName, "Ожидалось, что имя юзера в БД не изменится");
    }
}
