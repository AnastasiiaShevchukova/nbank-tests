package apiTests.iteration2;

import apiTests.BaseTest;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.DepositMoneyResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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
    }


    //Negative 1:
    public static Stream<Arguments> moneyInvalidDepositData() {
        return Stream.of(
                Arguments.of(-1, "Deposit amount must be at least 0.01"),
                Arguments.of(0, "Deposit amount must be at least 0.01"),
                Arguments.of(5001, "Deposit amount cannot exceed 5000")
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

    }
}
