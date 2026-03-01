package apiTests.iteration2;

import apiTests.BaseTest;
import generators.RandomData;
import models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositMoneyRequester;
import requests.TransferMoneyRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class TransferMoneyTest extends BaseTest {


    //Positive 1:
    public static Stream<Arguments> moneyValidTransferData() {
        return Stream.of(
                Arguments.of( 5000, 1, 1),
                Arguments.of( 5000, 3000, 1),
                Arguments.of( 5000, 9999, 2),
                Arguments.of( 5000, 10000, 2)
        );
    }
    // User can transfer money 1 - 10 000 rouble
    @ParameterizedTest
    @MethodSource("moneyValidTransferData")
    public void userCanDepositMoneyTest(Integer depositAmount, Integer transferAmount, Integer depositCount) {
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

        // создаем первый аккаунт и запоминаем его id
        int firstCreatedAccountId = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().jsonPath().getInt("id");

        // Выполняем депозит денег на первый аккаунт указанное количество раз (5000 или 10 000 или 15 000 и т.д)
        int totalDeposited = 0;
        for (int i = 0; i < depositCount; i++) {
            // депозит (максимум 5000 за раз)
            DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                    .id(firstCreatedAccountId)
                    .balance(depositAmount)
                    .build();
            new DepositMoneyRequester(RequestSpecs.authAsUserSpec(username, password), ResponseSpecs.requestReturnsOK())
                    .post(depositRequest);

            totalDeposited += depositAmount;
        }

        // создаем второй аккаунт и получаем его ID
        int secondCreatedAccountId = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().jsonPath().getInt("id");

        // Перевод денег между аккаунтами
        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(firstCreatedAccountId)
                .receiverAccountId(secondCreatedAccountId)
                .amount(transferAmount)
                .build();

        TransferMoneyResponse transferResponse = new TransferMoneyRequester(RequestSpecs.authAsUserSpec(username, password), ResponseSpecs.requestReturnsOK())
                .post(transferMoneyRequest)
                .extract().as(TransferMoneyResponse.class);

        softly.assertThat(transferResponse.getMessage()).isEqualTo("Transfer successful");
        softly.assertThat(transferResponse.getAmount()).isEqualTo(transferAmount);
    }


    //Negative 1:
    public static Stream<Arguments> moneyInvalidTransferData() {
        return Stream.of(
                Arguments.of( 5000, -10, 1, "Transfer amount must be at least 0.01"),
                Arguments.of( 5000, 0, 1, "Transfer amount must be at least 0.01"),
                Arguments.of( 5000, 10001, 2, "Transfer amount cannot exceed 10000"),
                Arguments.of( 5000, 5001, 1, "Invalid transfer: insufficient funds or invalid accounts")
        );
    }
    // User can NOT transfer money
    @ParameterizedTest
    @MethodSource("moneyInvalidTransferData")
    public void userCanNotDepositMoneyTest(Integer depositAmount, Integer transferAmount, Integer depositCount, String errorMsg) {
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

        // создаем первый аккаунт и запоминаем его id
        int firstCreatedAccountId = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().jsonPath().getInt("id");

        // Выполняем депозит денег на первый аккаунт указанное количество раз (5000 или 10 000 или 15 000 и т.д)
        int totalDeposited = 0;
        for (int i = 0; i < depositCount; i++) {
            // депозит (максимум 5000 за раз)
            DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                    .id(firstCreatedAccountId)
                    .balance(depositAmount)
                    .build();
            new DepositMoneyRequester(RequestSpecs.authAsUserSpec(username, password), ResponseSpecs.requestReturnsOK())
                    .post(depositRequest);

            totalDeposited += depositAmount;
        }

        // создаем второй аккаунт и получаем его ID
        int secondCreatedAccountId = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().jsonPath().getInt("id");

        // Перевод денег между аккаунтами
        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(firstCreatedAccountId)
                .receiverAccountId(secondCreatedAccountId)
                .amount(transferAmount)
                .build();

        new TransferMoneyRequester(RequestSpecs.authAsUserSpec(username, password), ResponseSpecs.requestReturnsBadRequestWithoutErrorKey(errorMsg))
                .post(transferMoneyRequest);
           }
}
