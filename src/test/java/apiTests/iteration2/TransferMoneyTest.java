package apiTests.iteration2;

import apiTests.BaseTest;
import api.models.CreateUserRequest;
import api.models.DepositMoneyRequest;
import api.models.TransferMoneyRequest;
import api.models.TransferMoneyResponse;
import api.models.comparison.ModelAssertions;
import common.annotations.APIBackend;
import common.annotations.APIVersion;
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

@APIVersion(APIBackend.DATABASE_FIX)
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
    public void userCanDepositMoneyTest(Integer depositAmount, Integer transferAmount, Integer depositCount) {
        CreateUserRequest createUser = AdminSteps.createUser();

        long firstCreatedAccountId = UserSteps.createAccount(createUser).getId();

        // Выполняем депозит денег на первый аккаунт указанное количество раз (5000 или 10 000 или 15 000 и т.д)
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
        long secondCreatedAccountId = UserSteps.createAccount(createUser).getId();

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(firstCreatedAccountId)
                .receiverAccountId(secondCreatedAccountId)
                .amount(transferAmount)
                .build();
        TransferMoneyResponse transferResponse = UserSteps.
                transferMoney(transferMoneyRequest, createUser);
        ModelAssertions.assertThatModels(transferMoneyRequest, transferResponse).match();

        // проверка, что баланс поменялся после трансфера
        UserSteps.checkAccountBalance(depositAmount * depositCount - transferAmount, createUser, firstCreatedAccountId);
    }


    //Negative 1:
    public static Stream<Arguments> moneyInvalidTransferData() {
        return Stream.of(
                Arguments.of(5000, -10, 1, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(5000, 0, 1, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(5000, 10001, 2, "Transfer amount cannot exceed 10000"),
                Arguments.of(5000, 5001, 1, "Invalid transfer: insufficient funds or invalid accounts")
        );
    }
    @ParameterizedTest(name = "User can NOT transfer money")
    @MethodSource("moneyInvalidTransferData")
    public void userCanNotTransferMoneyTest(Integer depositAmount, Integer transferAmount, Integer depositCount, String errorMsg) {
        CreateUserRequest createUser = AdminSteps.createUser();

        long firstCreatedAccountId = UserSteps.createAccount(createUser).getId();

        // Выполняем депозит денег на первый аккаунт указанное количество раз (5000 или 10 000 или 15 000 и т.д)
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
        long secondCreatedAccountId = UserSteps.createAccount(createUser).getId();
        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(firstCreatedAccountId)
                .receiverAccountId(secondCreatedAccountId)
                .amount(transferAmount)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUserSpec(createUser.getUsername(), createUser.getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsBadRequestWithoutErrorKey(errorMsg))
                .post(transferMoneyRequest);

        // проверка, что баланс не поменялся после трансфера
        UserSteps.checkAccountBalance(depositAmount * depositCount, createUser, firstCreatedAccountId);
    }
}
