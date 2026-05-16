package uiTests.iteration2;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.DepositMoneyRequest;
import common.annotations.APIBackend;
import common.annotations.APIVersion;
import common.annotations.Browsers;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.steps.UserSteps;
import ui.pages.BankAlerts;
import ui.pages.TransferMoney;
import uiTests.BaseUiTest;

import java.util.stream.Stream;

@APIVersion(APIBackend.DATABASE_FIX)
public class TransferMoneyUITest extends BaseUiTest {

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
    @UserSession()
    @Browsers({"chrome"})
    public void userCanTransferMoneyTest(Integer depositAmount, Integer transferAmount, Integer depositCount) {
        // Предусловие ШАГ 1: админ логинится в банке
        // Предусловие Шаг 2: админ создает юзера
        // Предусловие Шаг 3: юзер логинится в банке
        CreateUserRequest user = SessionStorage.getUser();
        // Предусловие Шаг 4: юзер создает первый аккаунт
        long firstCreatedAccountId = UserSteps.createAccount(user).getId();

        // Предусловие Шаг 5: юзер пополняет баланс первого аккаунта указанное количество раз
        int totalDeposited = 0;
        for (int i = 0; i < depositCount; i++) {
            // депозит (максимум 5000 за раз)
            DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                    .id(firstCreatedAccountId)
                    .balance(depositAmount)
                    .build();
            UserSteps.depositMoney(depositRequest, user);

            totalDeposited += depositAmount;
        }
        // Предусловие Шаг 6: юзер создает второй аккаунт
        CreateAccountResponse secondAccount = UserSteps.createAccount(user);
        String transferAmountString = transferAmount.toString();
        // ШАГИ ТЕСТА:
        // Тест Шаг1: Открыть страницу трансфера денег
        // Тест Шаг 2: Выбрать первый созданный аккаунт в селекте "Select Your Account:"
        // Тест Шаг 3: Ввести данные в поле "Recipient Name:"
        // Тест Шаг 4: Ввести данные второго аккаунта в поле "Recipient Account Number:"
        // Тест Шаг 5: Ввести данные о количестве переводимых денег в поле "Amount:"
        // Тест Шаг 6: Отметить true чекбокс "Confirm details are correct"
        // Тест Шаг 7: Нажать кнопку "🚀 Send Transfer"
        // Проверка UI, что трансфер успешен
        new TransferMoney().open().transferMoneyFromAccountToAccount(firstCreatedAccountId, secondAccount.getAccountNumber(), transferAmountString)
                .checkAlertMessageAndAccept(BankAlerts.SUCCESSFULLY_TRANSFERRED_MONEY_TO_ACCOUNT.getMessage(), transferAmount, secondAccount.getAccountNumber());

        // Проверка API, что трансфер успешен (баланс аккаунта изменился)
        UserSteps.checkAccountBalance(depositAmount * depositCount - transferAmount, user, firstCreatedAccountId);
    }

    //Negative 1:
    public static Stream<Arguments> moneyInvalid1TransferData() {
        return Stream.of(
                Arguments.of(5000, -10, 1),
                Arguments.of(5000, 0, 1)
        );
    }
    @ParameterizedTest(name = "User can NOT transfer money <=0")
    @MethodSource("moneyInvalid1TransferData")
    @UserSession()
    @Browsers({"chrome"})
    public void userCanNotTransferZeroMoneyTest(Integer depositAmount, Integer transferAmount, Integer depositCount) {
        // Предусловие ШАГ 1: админ логинится в банке
        // Предусловие Шаг 2: админ создает юзера
        // Предусловие Шаг 3: юзер логинится в банке
        CreateUserRequest user = SessionStorage.getUser();
        // Предусловие Шаг 4: юзер создает первый аккаунт
        long firstCreatedAccountId = UserSteps.createAccount(user).getId();

        // Предусловие Шаг 5: юзер пополняет баланс первого аккаунта указанное количество раз
        int totalDeposited = 0;
        for (int i = 0; i < depositCount; i++) {
            // депозит (максимум 5000 за раз)
            DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                    .id(firstCreatedAccountId)
                    .balance(depositAmount)
                    .build();
            UserSteps.depositMoney(depositRequest, user);

            totalDeposited += depositAmount;
        }
        // Предусловие Шаг 6: юзер создает второй аккаунт
        CreateAccountResponse secondAccount = UserSteps.createAccount(user);
        String transferAmountString = transferAmount.toString();
        // ШАГИ ТЕСТА:
        // Тест Шаг1: Открыть страницу трансфера денег
        // Тест Шаг 2: Выбрать первый созданный аккаунт в селекте "Select Your Account:"
        // Тест Шаг 3: Ввести данные в поле "Recipient Name:"
        // Тест Шаг 4: Ввести данные второго аккаунта в поле "Recipient Account Number:"
        // Тест Шаг 5: Ввести данные о количестве переводимых денег в поле "Amount:"
        // Тест Шаг 6: Отметить true чекбокс "Confirm details are correct"
        // Тест Шаг 7: Нажать кнопку "🚀 Send Transfer"
        // Проверка UI, что трансфер НЕ успешен
        new TransferMoney().open().transferMoneyFromAccountToAccount(firstCreatedAccountId, secondAccount.getAccountNumber(), transferAmountString)
                .checkAlertMessageAndAccept(BankAlerts.ERROR_TRANSFER_AMOUNT_MUST_BE_AT_LEAST_01.getMessage());

        // Проверка API, что трансфер НЕ успешен(баланс аккаунта не изменился)
        UserSteps.checkAccountBalance(depositAmount * depositCount, user, firstCreatedAccountId);
    }

    //Negative 2:
    public static Stream<Arguments> moneyInvalid2TransferData() {
        return Stream.of(
                Arguments.of(5000, 10001, 2)
        );
    }
    @ParameterizedTest(name = "User can NOT transfer money > 10 000")
    @MethodSource("moneyInvalid2TransferData")
    @UserSession()
    @Browsers({"chrome"})
    public void userCanNotTransferMoneyMore10000Test(Integer depositAmount, Integer transferAmount, Integer depositCount) {
        // Предусловие ШАГ 1: админ логинится в банке
        // Предусловие Шаг 2: админ создает юзера
        // Предусловие Шаг 3: юзер логинится в банке
        CreateUserRequest user = SessionStorage.getUser();
        // Предусловие Шаг 4: юзер создает первый аккаунт
        long firstCreatedAccountId = UserSteps.createAccount(user).getId();

        // Предусловие Шаг 5: юзер пополняет баланс первого аккаунта указанное количество раз
        int totalDeposited = 0;
        for (int i = 0; i < depositCount; i++) {
            // депозит (максимум 5000 за раз)
            DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                    .id(firstCreatedAccountId)
                    .balance(depositAmount)
                    .build();
            UserSteps.depositMoney(depositRequest, user);

            totalDeposited += depositAmount;
        }
        // Предусловие Шаг 6: юзер создает второй аккаунт
        CreateAccountResponse secondAccount = UserSteps.createAccount(user);
        String transferAmountString = transferAmount.toString();
        // ШАГИ ТЕСТА:
        // Тест Шаг1: Открыть страницу трансфера денег
        // Тест Шаг 2: Выбрать первый созданный аккаунт в селекте "Select Your Account:"
        // Тест Шаг 3: Ввести данные в поле "Recipient Name:"
        // Тест Шаг 4: Ввести данные второго аккаунта в поле "Recipient Account Number:"
        // Тест Шаг 5: Ввести данные о количестве переводимых денег в поле "Amount:"
        // Тест Шаг 6: Отметить true чекбокс "Confirm details are correct"
        // Тест Шаг 7: Нажать кнопку "🚀 Send Transfer"
        // Проверка UI, что трансфер НЕ успешен
        new TransferMoney().open().transferMoneyFromAccountToAccount(firstCreatedAccountId, secondAccount.getAccountNumber(), transferAmountString)
                .checkAlertMessageAndAccept(BankAlerts.ERROR_TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage());

        // Проверка API, что трансфер НЕ успешен(баланс аккаунта не изменился)
        UserSteps.checkAccountBalance(depositAmount * depositCount, user, firstCreatedAccountId);
    }


    //Negative 3:
    public static Stream<Arguments> moneyInvalid3TransferData() {
        return Stream.of(
                Arguments.of(5000, 5001, 1)
        );
    }
    @ParameterizedTest(name = "User can NOT transfer money")
    @MethodSource("moneyInvalid3TransferData")
    @UserSession()
    @Browsers({"chrome"})
    public void userCanNotTransferMoneyTest(Integer depositAmount, Integer transferAmount, Integer depositCount) {
        // Предусловие ШАГ 1: админ логинится в банке
        // Предусловие Шаг 2: админ создает юзера
        // Предусловие Шаг 3: юзер логинится в банке
        CreateUserRequest user = SessionStorage.getUser();
        // Предусловие Шаг 4: юзер создает первый аккаунт
        long firstCreatedAccountId = UserSteps.createAccount(user).getId();

        // Предусловие Шаг 5: юзер пополняет баланс первого аккаунта указанное количество раз
        int totalDeposited = 0;
        for (int i = 0; i < depositCount; i++) {
            // депозит (максимум 5000 за раз)
            DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                    .id(firstCreatedAccountId)
                    .balance(depositAmount)
                    .build();
            UserSteps.depositMoney(depositRequest, user);

            totalDeposited += depositAmount;
        }
        // Предусловие Шаг 6: юзер создает второй аккаунт
        CreateAccountResponse secondAccount = UserSteps.createAccount(user);
        String transferAmountString = transferAmount.toString();
        // ШАГИ ТЕСТА:
        // Тест Шаг1: Открыть страницу трансфера денег
        // Тест Шаг 2: Выбрать первый созданный аккаунт в селекте "Select Your Account:"
        // Тест Шаг 3: Ввести данные в поле "Recipient Name:"
        // Тест Шаг 4: Ввести данные второго аккаунта в поле "Recipient Account Number:"
        // Тест Шаг 5: Ввести данные о количестве переводимых денег в поле "Amount:"
        // Тест Шаг 6: Отметить true чекбокс "Confirm details are correct"
        // Тест Шаг 7: Нажать кнопку "🚀 Send Transfer"
        // Проверка UI, что трансфер НЕ успешен
        new TransferMoney().open().transferMoneyFromAccountToAccount(firstCreatedAccountId, secondAccount.getAccountNumber(), transferAmountString)
                .checkAlertMessageAndAccept(BankAlerts.ERROR_INVALID_TRANSFER_INSUFFICIENT_FUNDS_OR_INVALID_ACCOUNTS.getMessage());

        // Проверка API, что трансфер НЕ успешен(баланс аккаунта не изменился)
        UserSteps.checkAccountBalance(depositAmount * depositCount, user, firstCreatedAccountId);
    }
}
