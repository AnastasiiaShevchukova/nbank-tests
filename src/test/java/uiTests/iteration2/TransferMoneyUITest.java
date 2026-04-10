package uiTests.iteration2;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;
import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TransferMoneyUITest {

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
    public static Stream<Arguments> moneyValidTransferData() {
        return Stream.of(
                Arguments.of(5000, 1, 1),
                Arguments.of(5000, 3000, 1),
                Arguments.of(5000, 9999, 2),
                Arguments.of(5000, 10000, 2)
        );
    }
    @ParameterizedTest(name = "User can transfer money 1 - 10 000 rouble")
    @MethodSource("moneyValidTransferData")
    public void userCanTransferMoneyTest(Integer depositAmount, Integer transferAmount, Integer depositCount) {
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

        // Предусловие Шаг 4: юзер создает первый аккаунт
        long firstCreatedAccountId = UserSteps.createAccount(createUser).getId();

        // Предусловие Шаг 5: юзер пополняет баланс первого аккаунта указанное количество раз
        int totalDeposited = 0;
        for (int i = 0; i < depositCount; i++) {
            // депозит (максимум 5000 за раз)
            DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                    .id(firstCreatedAccountId)
                    .balance(depositAmount)
                    .build();
            UserSteps.depositMoney(depositRequest, createUser);

            totalDeposited += depositAmount;
        }
        // Предусловие Шаг 6: юзер создает второй аккаунт
        long secondCreatedAccountId = UserSteps.createAccount(createUser).getId();

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // Тест Шаг 1: Клик "Make a Transfer" в меню на дашборде юзера
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        // Тест Шаг 2: Выбрать первый созданный аккаунт в селекте "Select Your Account:"
        $(".account-selector").selectOptionByValue(String.valueOf(firstCreatedAccountId));
        // Тест Шаг 3: Ввести данные в поле "Recipient Name:"
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys("To me");
        // Тест Шаг 4: Ввести данные второго аккаунта в поле "Recipient Account Number:"
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys("ACC" + secondCreatedAccountId);
        // Тест Шаг 5: Ввести данные о количестве переводимых денег в поле "Amount:"
        String transferAmountString = transferAmount.toString();
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(transferAmountString);
        // Тест Шаг 6: Отметить true чекбокс "Confirm details are correct"
        $("#confirmCheck").click();
        // Тест Шаг 7: Нажать кнопку "🚀 Send Transfer"
        $$("button").findBy(exactText("🚀 Send Transfer")).click();

        // Проверка UI, что трансфер успешен
        Alert alertAccount = switchTo().alert();
        String alertAccountText = alertAccount.getText();
        assertThat(alertAccountText).contains("✅ Successfully transferred $" + transferAmount + " to account ACC" + secondCreatedAccountId + "!");
        alertAccount.accept();

        // Проверка API, что трансфер успешен (баланс аккаунта изменился)
        UserSteps.checkAccountBalance(depositAmount * depositCount - transferAmount, createUser, firstCreatedAccountId);
    }

    //Negative 1:
    public static Stream<Arguments> moneyInvalidTransferData() {
        return Stream.of(
                Arguments.of(5000, -10, 1),
                Arguments.of(5000, 0, 1),
                Arguments.of(5000, 10001, 2),
                Arguments.of(5000, 5001, 1)
        );
    }
    @ParameterizedTest(name = "User can NOT transfer money")
    @MethodSource("moneyInvalidTransferData")
    public void userCanNotTransferMoneyTest(Integer depositAmount, Integer transferAmount, Integer depositCount) {
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

        // Предусловие Шаг 4: юзер создает первый аккаунт
        long firstCreatedAccountId = UserSteps.createAccount(createUser).getId();

        // Предусловие Шаг 5: юзер пополняет баланс первого аккаунта указанное количество раз
        int totalDeposited = 0;
        for (int i = 0; i < depositCount; i++) {
            // депозит (максимум 5000 за раз)
            DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                    .id(firstCreatedAccountId)
                    .balance(depositAmount)
                    .build();
            UserSteps.depositMoney(depositRequest, createUser);

            totalDeposited += depositAmount;
        }
        // Предусловие Шаг 6: юзер создает второй аккаунт
        long secondCreatedAccountId = UserSteps.createAccount(createUser).getId();

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // Тест Шаг 1: Клик "Make a Transfer" в меню на дашборде юзера
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        // Тест Шаг 2: Выбрать первый созданный аккаунт в селекте "Select Your Account:"
        $(".account-selector").selectOptionByValue(String.valueOf(firstCreatedAccountId));
        // Тест Шаг 3: Ввести данные в поле "Recipient Name:"
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys("To me");
        // Тест Шаг 4: Ввести данные второго аккаунта в поле "Recipient Account Number:"
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys("ACC" + secondCreatedAccountId);
        // Тест Шаг 5: Ввести данные о количестве переводимых денег в поле "Amount:"
        String transferAmountString = transferAmount.toString();
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(transferAmountString);
        // Тест Шаг 6: Отметить true чекбокс "Confirm details are correct"
        $("#confirmCheck").click();
        // Тест Шаг 7: Нажать кнопку "🚀 Send Transfer"
        $$("button").findBy(exactText("🚀 Send Transfer")).click();

        // Проверка UI, что трансфер НЕ успешен
        Alert alertAccount = switchTo().alert();
        String alertAccountText = alertAccount.getText();

        boolean isValidAmountError = alertAccountText.contains("❌ Error: Transfer amount must be at least 0.01");
        boolean isLimitError = alertAccountText.contains("❌ Error: Transfer amount cannot exceed 10000");
        boolean isValidLimitError = alertAccountText.contains("❌ Error: Invalid transfer: insufficient funds or invalid accounts");

        assertThat(isValidAmountError || isLimitError  || isValidLimitError)
                .as("Ожидалась ошибка валидации трансфера")
                .isTrue();
        alertAccount.accept();

        // Проверка API, что трансфер НЕ успешен(баланс аккаунта не изменился)
        UserSteps.checkAccountBalance(depositAmount * depositCount, createUser, firstCreatedAccountId);
    }
}
