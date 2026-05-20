package apiTests.iteration2;

import api.models.*;
import api.requests.steps.DataBaseSteps;
import apiTests.BaseTest;
import api.models.comparison.ModelAssertions;
import common.annotations.APIBackend;
import common.annotations.APIVersion;
import common.annotations.FraudCheckMock;
import common.annotations.PrepareUsers;
import common.extensions.FraudCheckWireMockExtension;
import common.extensions.TimingExtension;
import common.storage.SessionStorage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@APIVersion(APIBackend.DATABASE_FIX)
@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferMoneyTest extends BaseTest {


    //Positive 1:
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
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    @PrepareUsers()
    public void userCanDepositMoneyTest(Integer depositAmount, Integer transferAmount, Integer depositCount) {
        UserSteps userSteps1 = SessionStorage.getSteps(1);
        CreateUserRequest user1= SessionStorage.getUser();
        long account1 = SessionStorage.getUserAccount(1).getId();

        // Выполняем депозит денег на первый аккаунт указанное количество раз (5000 или 10 000 или 15 000 и т.д)
        int totalDeposited = 0;
        for (int i = 0; i < depositCount; i++) {
            DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                    .id(0)
                    .balance(0.0)
                    .accountId(account1)
                    .amount(depositAmount)
                    .description("Test deposit")
                    .build();
            UserSteps.depositMoney(depositRequest, user1);

            totalDeposited += depositAmount;
        }

        long account2 = UserSteps.createAccount(user1).getId();
        // Шаги теста:
        // Попытка перевода денег с проверкой на фрод
        TransferMoneyResponse transferResponse = userSteps1.transferWithFraudCheck(
                account1,
                account2,
                transferAmount
        );

        softly.assertThat(transferResponse).isNotNull();
        TransferMoneyResponse expectedResponse = TransferMoneyResponse.builder()
                .status("APPROVED")
                .message("Transfer approved and processed immediately")
                .amount(transferAmount)
                .senderAccountId(account1)
                .receiverAccountId(account2)
                .fraudRiskScore(0.2)
                .fraudReason("Low risk transaction")
                .requiresManualReview(false)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();

        // проверка через АПИ, что баланс поменялся после трансфера
        Double expectedBalance = Double.valueOf(depositAmount * depositCount - transferAmount);
        UserSteps.checkAccountBalance(expectedBalance, user1, account1);

        // Проверка через БД
        Double actualBalance = DataBaseSteps.getAccountBalanceByAccountId(account1).getBalance();
        assertEquals(expectedBalance, actualBalance, 0.01, "Ожидалось, что баланс в БД отправляющего аккаунта уменьшится на сумму трансфера");
    }


    //Negative 1:
    public static Stream<Arguments> moneyInvalidTransferData() {
        return Stream.of(
                Arguments.of(5000, -10, 1, "Invalid accounts or amount"),
                Arguments.of(5000, 0, 1, "Invalid accounts or amount"),
                Arguments.of(5000, 10001, 2, "Transfer amount cannot exceed 10000"),
                Arguments.of(5000, 5001, 1, "Insufficient funds")
        );
    }
    @ParameterizedTest(name = "User can NOT transfer money")
    @MethodSource("moneyInvalidTransferData")
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    @PrepareUsers()
    public void userCanNotTransferMoneyTest(Integer depositAmount, Integer transferAmount, Integer depositCount, String errorMsg) {
        UserSteps userSteps1 = SessionStorage.getSteps(1);
        CreateUserRequest user1= SessionStorage.getUser();
        long account1 = SessionStorage.getUserAccount(1).getId();

        // Выполняем депозит денег на первый аккаунт указанное количество раз (5000 или 10 000 или 15 000 и т.д)
        int totalDeposited = 0;
        for (int i = 0; i < depositCount; i++) {
            // депозит (максимум 5000 за раз)
            DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                    .id(0)
                    .balance(0.0)
                    .accountId(account1)
                    .amount(depositAmount)
                    .description("Test deposit")
                    .build();
            UserSteps.depositMoney(depositRequest, user1);

            totalDeposited += depositAmount;
        }
        long account2 = UserSteps.createAccount(user1).getId();
        // Шаги теста:
        // Попытка перевода денег с проверкой на фрод
        TransferMoneyResponse transferResponse = userSteps1.transferWithFraudCheckReturns400(
                account1,
                account2,
                transferAmount,
                errorMsg
        );
        softly.assertThat(transferResponse).isNotNull();
        TransferMoneyResponse expectedResponse = TransferMoneyResponse.builder()
                .status(null)
                .message(errorMsg)
                .amount(0)
                .senderAccountId(0)
                .receiverAccountId(0)
                .fraudRiskScore(0.0)
                .fraudReason(null)
                .requiresManualReview(false)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();

        // проверка через АПИ, что баланс не поменялся после трансфера
        Double expectedBalance = Double.valueOf(depositAmount * depositCount);
        UserSteps.checkAccountBalance(expectedBalance, user1, account1);

        // Проверка через БД
        Double actualBalance = DataBaseSteps.getAccountBalanceByAccountId(account1).getBalance();
        assertEquals(expectedBalance, actualBalance, 0.01, "Ожидалось, что баланс в БД отправляющего аккаунта не изменится");
    }

}
