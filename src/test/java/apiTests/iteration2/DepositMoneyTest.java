package apiTests.iteration2;

import api.requests.steps.AdminSteps;
import api.requests.steps.DataBaseSteps;
import apiTests.BaseTest;
import api.models.CreateUserRequest;
import api.models.DepositMoneyRequest;
import api.models.DepositMoneyResponse;
import api.models.comparison.ModelAssertions;
import common.annotations.APIBackend;
import common.annotations.APIVersion;
import common.annotations.PrepareUsers;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@APIVersion(APIBackend.DATABASE_FIX)
public class DepositMoneyTest extends BaseTest {

    //Positive 1:
    @ParameterizedTest(name = "User can deposit money 1 - 5000 rouble")
    @ValueSource(ints = {1, 2500, 4999, 5000})
    public void userCanDepositMoneyTest(int depositAmount) {
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        long createdAccountId = UserSteps.createAccount(createUserRequest).getId();

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(createdAccountId)
                .balance(depositAmount)
                .build();
        DepositMoneyResponse depositResponse = UserSteps.depositMoney(depositRequest, createUserRequest);

        ModelAssertions.assertThatModels(depositRequest, depositResponse).match();

        // Проверка через АПИ, что баланс изменился
        UserSteps.checkAccountBalance(depositAmount, createUserRequest, createdAccountId);

        // Проверка через БД
        Double expectedBalance = Double.valueOf(depositAmount);
        Double actualBalance = DataBaseSteps.getAccountBalanceByAccountNumber(depositResponse.getAccountNumber()).getBalance();

        assertEquals(expectedBalance, actualBalance, 0.01, "Баланс в базе данных не соответствует балансу из POST запроса на депозит");

    }


    //Negative 1:
    public static Stream<Arguments> moneyInvalidDepositData() {
        return Stream.of(
                Arguments.of(-1, "Invalid field types: accountId must be integer, amount must be number"),
                Arguments.of(0, "Invalid field types: accountId must be integer, amount must be number"),
                Arguments.of(5001, "Invalid field types: accountId must be integer, amount must be number")
        );
    }

    @ParameterizedTest(name = "User can not deposit money < 0 or > 5000 rouble")
    @MethodSource("moneyInvalidDepositData")
    public void userCanNotDepositMoneyTest(Integer depositAmount, String errorMsg) {
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        long createdAccountId = UserSteps.createAccount(createUserRequest).getId();

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(createdAccountId)
                .balance(depositAmount)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsBadRequestWithErrorMessage(errorMsg))
                .post(depositRequest);

        // Проверка через АПИ, что баланс не изменился
        UserSteps.checkAccountBalance(0, createUserRequest, createdAccountId);

        // Проверка через БД
        Double actualBalance = DataBaseSteps.getAccountBalanceByAccountId(createdAccountId).getBalance();
        assertEquals(0.0, actualBalance, 0.01, "Баланс в базе данных должен быть равен 0");
    }

    //Negative 2
    @Test
    @DisplayName("User can not deposit money into ANOTHER ACCOUNT")
    public void userCanNotDepositMoneyIntoAnotherAccountTest() {
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        long createdAccountId = UserSteps.createAccount(createUserRequest).getId();

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(createdAccountId + 20000)
                .balance(10)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsBadRequestWithErrorMessage("Invalid field types: accountId must be integer, amount must be number"))
                .post(depositRequest);

        // Проверка через АПИ, что баланс не изменился
        UserSteps.checkAccountBalance(0, createUserRequest, createdAccountId);

        // Проверка через БД
        Double actualBalance = DataBaseSteps.getAccountBalanceByAccountId(createdAccountId).getBalance();
        assertEquals(0.0, actualBalance, 0.01, "Баланс в базе данных должен быть равен 0");

    }
}
