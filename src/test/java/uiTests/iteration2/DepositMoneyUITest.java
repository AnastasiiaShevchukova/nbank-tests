package uiTests.iteration2;

import api.models.CreateUserRequest;
import api.requests.steps.DataBaseSteps;
import common.annotations.APIBackend;
import common.annotations.APIVersion;
import common.annotations.Browsers;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.steps.UserSteps;
import ui.pages.BankAlerts;
import ui.pages.DepositMoney;
import uiTests.BaseUiTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@APIVersion(APIBackend.DATABASE_FIX)
public class DepositMoneyUITest extends BaseUiTest {

    //Позитив
    @ParameterizedTest(name = "User can deposit money 1 - 5000 rouble")
    @ValueSource(strings = {"1", "2500", "4999", "5000"})
    @UserSession()
    @Browsers({"chrome"})
    public void userCanDepositMoneyTest(String depositAmount) {
        // Предусловие ШАГ 1: админ логинится в банке
        // Предусловие Шаг 2: админ создает юзера
        // Предусловие Шаг 3: юзер логинится в банке
        // Предусловие Шаг 4: юзер создает аккаунт
        CreateUserRequest user = SessionStorage.getUser();
        long createdAccountId =  UserSteps.createAccount(user).getId();
        // ШАГИ ТЕСТА:
        // Открыть страницу депозита денег
        // Выбрать созданный аккаунт в селекте "Select Account:"
        // Ввести данные о количестве денег в поле "Enter Amount:"
        // Нажать кнопку "💵 Deposit"
        new DepositMoney().open().depositMoneyToAccount(createdAccountId, depositAmount)
                .checkAlertMessageAndAccept(BankAlerts.SUCCESSFULLY_DEPOSITED_MONEY_TO_ACCOUNT.getMessage(), depositAmount, createdAccountId);

        // Проверка API, что депозит успешен (баланс аккаунта изменился)
        Double expectedBalance = Double.valueOf(depositAmount);
        UserSteps.checkAccountBalance(expectedBalance, user, createdAccountId);

        // Проверка через БД
        //Double actualBalance = DataBaseSteps.getAccountBalanceByAccountId(createdAccountId).getBalance();
        //assertEquals(expectedBalance, actualBalance, 0.01, "Баланс в базе данных не соответствует балансу из POST запроса на депозит");
    }

    //Negative 1:
    @ParameterizedTest(name = "User can not deposit money <= 0 5000 rouble")
    @ValueSource(strings = {"-1", "0"})
    @UserSession()
    @Browsers({"chrome"})
    public void userCanNotDepositMoneyTest(String depositAmount) {
        // Предусловие ШАГ 1: админ логинится в банке
        // Предусловие Шаг 2: админ создает юзера
        // Предусловие Шаг 3: юзер логинится в банке
        // Предусловие Шаг 4: юзер создает аккаунт
        CreateUserRequest user = SessionStorage.getUser();
        long createdAccountId =  UserSteps.createAccount(user).getId();
        // ШАГИ ТЕСТА:
        // Открыть страницу депозита денег
        // Выбрать созданный аккаунт в селекте "Select Account:"
        // Ввести данные о количестве денег в поле "Enter Amount:"
        // Нажать кнопку "💵 Deposit"
        // Проверка UI, что депозит НЕ успешен
        new DepositMoney().open().depositMoneyToAccount(createdAccountId, depositAmount)
                .checkAlertMessageAndAccept(BankAlerts.PLEASE_ENTER_A_VALID_AMOUNT.getMessage(), depositAmount, createdAccountId);

        // Проверка API, что депозит НЕ успешен (баланс аккаунта равен 0)
        UserSteps.checkAccountBalance(0, user, createdAccountId);

        // Проверка через БД
        //Double actualBalance = DataBaseSteps.getAccountBalanceByAccountId(createdAccountId).getBalance();
        //assertEquals(0.0, actualBalance, 0.01, "Баланс в базе данных должен быть равен 0");
    }

    //Negative 2:
    @ParameterizedTest(name = "User can not deposit money > 5000 rouble")
    @ValueSource(strings = {"5001"})
    @UserSession()
    @Browsers({"chrome"})
    public void userCanNotDepositMoneyMore5000Test(String depositAmount) {
        // Предусловие ШАГ 1: админ логинится в банке
        // Предусловие Шаг 2: админ создает юзера
        // Предусловие Шаг 3: юзер логинится в банке
        // Предусловие Шаг 4: юзер создает аккаунт
        CreateUserRequest user = SessionStorage.getUser();
        long createdAccountId =  UserSteps.createAccount(user).getId();
        // ШАГИ ТЕСТА:
        // Открыть страницу депозита денег
        // Выбрать созданный аккаунт в селекте "Select Account:"
        // Ввести данные о количестве денег в поле "Enter Amount:"
        // Нажать кнопку "💵 Deposit"
        // Проверка UI, что депозит НЕ успешен
        new DepositMoney().open().depositMoneyToAccount(createdAccountId, depositAmount)
                .checkAlertMessageAndAccept(BankAlerts.PLEASE_DEPOSIT_LESS_OR_EQUAL_TO_5000.getMessage(), depositAmount, createdAccountId);

        // Проверка API, что депозит НЕ успешен (баланс аккаунта равен 0)
        UserSteps.checkAccountBalance(0, user, createdAccountId);

        // Проверка через БД
        //Double actualBalance = DataBaseSteps.getAccountBalanceByAccountId(createdAccountId).getBalance();
        //assertEquals(0.0, actualBalance, 0.01, "Баланс в базе данных должен быть равен 0");
    }
}
