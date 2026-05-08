package uiTests.iteration2;

import com.codeborne.selenide.Selenide;
import api.models.CreateUserRequest;
import common.annotations.Browsers;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.steps.UserSteps;
import ui.pages.BankAlerts;
import ui.pages.EditProfile;
import uiTests.BaseUiTest;


import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class ChangeNameUITest extends BaseUiTest {


    // Positive 1:
    @Test
    @DisplayName("User can change name")
    @UserSession()
    @Browsers({"chrome"})
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
        new EditProfile().open().updateUserName("John Smith")
                .checkAlertMessageAndAccept(BankAlerts.NAME_UPDATED_SUCCESSFULLY.getMessage());

        Selenide.open("/dashboard");
        $(".user-name").shouldHave(text("John Smith")).shouldBe(visible);

        // Проверка API, что имя поменялось
        UserSteps.checkName(user, "John Smith", "Ожидалось, что имя пользователя изменится на новое значение");
    }

    //Negative 1:
    @ParameterizedTest(name = "User can NOT change name {0}")
    @ValueSource(strings = {"John", "John John John", "", "     "})
    @UserSession()
    @Browsers({"chrome"})
    public void userCanNotChangeNameTest(String newNameValue) {
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
                .checkAlertMessageAndAccept(BankAlerts.PLEASE_ENTER_A_VALID_NAME.getMessage());

        Selenide.open("/dashboard");
        $(".user-name").shouldHave(text("Noname")).shouldBe(visible);

        // Проверка API, что имя юзера не поменялось
        UserSteps.checkName(user, null, "Ожидалось, что имя юзера не поменяется");
    }

    //Negative 2:
    @ParameterizedTest(name = "User can NOT change name {0}")
    @ValueSource(strings = {"123 123", "^$# **& ^$# **&"})
    @UserSession()
    @Browsers({"chrome"})
    public void userCanNotChange2NameTest(String newNameValue) {
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
                .checkAlertMessageAndAccept(BankAlerts.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getMessage());

        Selenide.open("/dashboard");
        $(".user-name").shouldHave(text("Noname")).shouldBe(visible);

        // Проверка API, что имя юзера не поменялось
        UserSteps.checkName(user, null, "Ожидалось, что имя юзера не поменяется");
    }
}
