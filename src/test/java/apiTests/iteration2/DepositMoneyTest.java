package apiTests.iteration2;

import apiTests.BaseTest;
import generators.RandomData;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.DepositMoneyResponse;
import models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositMoneyRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class DepositMoneyTest extends BaseTest {

    //Positive 1:
    // User can deposit money 1 - 5000 rouble
    @ParameterizedTest
    @ValueSource(ints = {1, 2500, 4999, 5000})
    public void userCanDepositMoneyTest(int depositAmount) {
        //создание пользователя
        String username = RandomData.getUserName();
        String password = RandomData.getUserPassword();

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // создаем аккаунт и запоминаем его id
        int createdAccountId = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().jsonPath().getInt("id");

        // депозит
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(createdAccountId)
                .balance(depositAmount)
                .build();

        DepositMoneyResponse depositResponse = new DepositMoneyRequester(RequestSpecs.authAsUserSpec(username, password), ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract().as(DepositMoneyResponse.class);

        softly.assertThat(depositResponse.getId()).isNotNull();
        softly.assertThat(depositResponse.getId()).isEqualTo(createdAccountId);
        softly.assertThat(depositResponse.getBalance()).isEqualTo(depositAmount);
    }


    //Negative 1:
    public static Stream<Arguments> moneyInvalidDepositData() {
        return Stream.of(
                Arguments.of(-1, "Deposit amount must be at least 0.01"),
                Arguments.of(0, "Deposit amount must be at least 0.01"),
                Arguments.of(5001, "Deposit amount cannot exceed 5000")
        );
    }

    // User can not deposit money < 0 or > 5000 rouble
    @ParameterizedTest
    @MethodSource("moneyInvalidDepositData")
    public void userCanNotDepositMoneyTest(Integer depositAmount, String errorMsg) {
        //создание пользователя
        String username = RandomData.getUserName();
        String password = RandomData.getUserPassword();

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // создаем аккаунт и запоминаем его id
        int createdAccountId = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().jsonPath().getInt("id");

        // депозит
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(createdAccountId)
                .balance(depositAmount)
                .build();

        new DepositMoneyRequester(RequestSpecs.authAsUserSpec(username, password), ResponseSpecs.requestReturnsBadRequestWithoutErrorKey(errorMsg))
                .post(depositRequest);
    }

    //Negative 2
    // User can not deposit money into ANOTHER ACCOUNT
    @Test
    public void userCanNotDepositMoneyIntoAnotherAccountTest() {
        //создание пользователя
        String username = RandomData.getUserName();
        String password = RandomData.getUserPassword();

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // создаем аккаунт и запоминаем его id
        int createdAccountId = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().jsonPath().getInt("id");

        // депозит
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(createdAccountId + 20000)
                .balance(10)
                .build();

        new DepositMoneyRequester(RequestSpecs.authAsUserSpec(username, password), ResponseSpecs.requestReturnsForbidden("Unauthorized access to account"))
                .post(depositRequest);

    }
}
