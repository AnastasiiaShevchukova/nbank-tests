package uiTests.iteration2;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.Alert;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ChangeNameUITest {

    @BeforeAll
    public static void setupSelenoid(){
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.0.51:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

    // Positive 1:
    @Test
    @DisplayName("User can change name")
    public void userCanChangeNameTest() {
        // Предусловие ШАГ 1: админ логинится в банке
        // Предусловие Шаг 2: админ создает юзера
        // Предусловие Шаг 3: юзер логинится в банке
        CreateUserRequest createUser = AdminSteps.createUser();

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(createUser.getUsername()).password(createUser.getPassword()).build())
                .extract()
                .header("Authorization");
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // Тест Шаг 1: Открыть меню изменения имени юзера
        $("span.user-name").click();
        // Тест Шаг 2: Ввести новое имя пользователя
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys("John Smith");
        // Тест Шаг 3: Нажать кнопку "💾 Save Changes"
        $$("button").findBy(exactText("\uD83D\uDCBE Save Changes")).click();

        // Проверка UI, что имя пользователя обновилось успешно
        Alert alertAccount = switchTo().alert();
        String alertAccountText = alertAccount.getText();
        assertThat(alertAccountText).contains("✅ Name updated successfully!");
        alertAccount.accept();

        Selenide.open("/dashboard");
        $(".user-name").shouldHave(text("John Smith")).shouldBe(visible);

        // Проверка API, что имя поменялось
        UserSteps.checkName(createUser, "John Smith", "Ожидалось, что имя пользователя изменится на новое значение");
    }

    //Negative 1:
    @ParameterizedTest(name = "User can NOT change name")
    @ValueSource(strings = {"John", "John John John", "123 123", "^$# **& ^$# **&", "", "     "})
    public void userCanNotChangeNameTest(String newNameValue) {
        // Предусловие ШАГ 1: админ логинится в банке
        // Предусловие Шаг 2: админ создает юзера
        // Предусловие Шаг 3: юзер логинится в банке
        CreateUserRequest createUser = AdminSteps.createUser();

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(createUser.getUsername()).password(createUser.getPassword()).build())
                .extract()
                .header("Authorization");
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // Тест Шаг 1: Открыть меню изменения имени юзера
        $("span.user-name").click();
        // Тест Шаг 2: Ввести новое имя пользователя
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(newNameValue);
        // Тест Шаг 3: Нажать кнопку "💾 Save Changes"
        $$("button").findBy(exactText("\uD83D\uDCBE Save Changes")).click();

        // Проверка UI, что изменение имени юзера НЕ успешно
        Alert alertAccount = switchTo().alert();
        String alertAccountText = alertAccount.getText();
        assertThat(alertAccountText).contains("❌ Please enter a valid name.");
        alertAccount.accept();

        Selenide.open("/dashboard");
        $(".user-name").shouldHave(text("Noname")).shouldBe(visible);

        // Проверка API, что имя юзера не поменялось
        UserSteps.checkName(createUser, null, "Ожидалось, что имя юзера не поменяется");
    }
}
