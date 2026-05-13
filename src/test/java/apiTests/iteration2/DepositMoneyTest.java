package apiTests.iteration2;

import apiTests.BaseTest;
import api.models.CreateUserRequest;
import api.models.DepositMoneyRequest;
import api.models.DepositMoneyResponse;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.stream.Stream;

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

        // Проверка, что баланс изменился
        UserSteps.checkAccountBalance(depositAmount, createUserRequest, createdAccountId);
    }


    //Negative 1:
    public static Stream<Arguments> moneyInvalidDepositData() {
        return Stream.of(
                Arguments.of(-1, "Invalid account or amount"),
                Arguments.of(0, "Invalid account or amount"),
                Arguments.of(5001, "Deposit amount exceeds the 5000 limit")
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
                ResponseSpecs.requestReturnsBadRequestWithoutErrorKey(errorMsg))
                .post(depositRequest);

        // Проверка, что баланс не изменился
        UserSteps.checkAccountBalance(0, createUserRequest, createdAccountId);
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
                ResponseSpecs.requestReturnsForbidden("Unauthorized access to account"))
                .post(depositRequest);

        // Проверка, что баланс не изменился
        UserSteps.checkAccountBalance(0, createUserRequest, createdAccountId);

    }
}
