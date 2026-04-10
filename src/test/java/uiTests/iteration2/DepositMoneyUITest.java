package uiTests.iteration2;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DepositMoneyUITest {

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

    //Позитив
    @ParameterizedTest(name = "User can deposit money 1 - 5000 rouble")
    @ValueSource(strings = {"1", "2500", "4999", "5000"})
    public void userCanDepositMoneyTest(String depositAmount) {
        // Предусловие ШАГ 1: админ логинится в банке
        // Предусловие Шаг 2: админ создает юзера
        // Предусловие Шаг 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser();

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        // Предусловие Шаг 4: юзер создает аккаунт
        long createdAccountId = UserSteps.createAccount(user).getId();

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // Тест Шаг 1: Клик "Deposit Money" в меню на дашборде юзера
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        // Тест Шаг 2: Выбрать созданный аккаунт в селекте "Select Account:"
        $(".account-selector").selectOption("ACC" + createdAccountId + " (Balance: $0.00)");
        // Тест Шаг 3: Ввести данные о количестве денег в поле "Enter Amount:"
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(depositAmount);
        // Тест Шаг 4: Нажать кнопку "💵 Deposit"
        $$("button").findBy(text("Deposit")).shouldHave(exactText("💵 Deposit")).click();

        // Проверка, что депозит успешен на UI
        Alert alertAccount = switchTo().alert();
        String alertAccountText = alertAccount.getText();
        assertThat(alertAccountText).contains("✅ Successfully deposited $" + depositAmount + " to account ACC" + createdAccountId + "!");
        alertAccount.accept();

        // Проверка, что депозит успешен на API (баланс аккаунта изменился)
        double depositAmountDouble = Double.parseDouble(depositAmount);
        UserSteps.checkAccountBalance(depositAmountDouble, user, createdAccountId);
    }

    //Negative 1:
    @ParameterizedTest(name = "User can not deposit money < 0 or > 5000 rouble")
    @ValueSource(strings = {"-1", "0", "5001"})
    public void userCanNotDepositMoneyTest(String depositAmount) {
        // Предусловие ШАГ 1: админ логинится в банке
        // Предусловие Шаг 2: админ создает юзера
        // Предусловие Шаг 3: юзер логинится в банке
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(createUserRequest.getUsername()).password(createUserRequest.getPassword()).build())
                .extract()
                .header("Authorization");
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        // Предусловие Шаг 4: юзер создает аккаунт
        long createdAccountId = UserSteps.createAccount(createUserRequest).getId();
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // Тест Шаг 1: Клик "Deposit Money" в меню на дашборде юзера
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        // Тест Шаг 2: Выбрать созданный аккаунт в селекте "Select Account:"
        $(".account-selector").selectOption("ACC" + createdAccountId + " (Balance: $0.00)");
        // Тест Шаг 3: Ввести данные о количестве денег в поле "Enter Amount:"
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(depositAmount);
        // Тест Шаг 4: Нажать кнопку "💵 Deposit"
        $$("button").findBy(text("Deposit")).shouldHave(exactText("💵 Deposit")).click();

        // Проверка, что депозит НЕ успешен на UI
        Alert alertAccount = switchTo().alert();
        String alertAccountText = alertAccount.getText();

        boolean isValidAmountError = alertAccountText.contains("❌ Please enter a valid amount.");
        boolean isLimitError = alertAccountText.contains("❌ Please deposit less or equal to 5000$.");

        assertThat(isValidAmountError || isLimitError)
                .as("Ожидалась ошибка валидации депозита")
                .isTrue();
        alertAccount.accept();

        // Проверка, что депозит НЕ успешен на API (баланс аккаунта равен 0)
        UserSteps.checkAccountBalance(0, createUserRequest, createdAccountId);
    }
}
