package api.requests.steps;

import api.models.*;
import common.helpers.StepLogger;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Assertions;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.Arrays;
import java.util.List;


public class UserSteps {

    private String username;
    private String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static CreateAccountResponse createAccount(CreateUserRequest user) {
        CreateAccountResponse response = new ValidatedCrudRequester<CreateAccountResponse>
                (RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated())
                .post(null);
        return response;
    }

    public static List<GetAllCustomerAccountsResponse> getAllCustomerAccounts(String username, String password) {
        ValidatableResponse response = new CrudRequester(
                RequestSpecs.authAsUserSpec(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK())
                .get();

        GetAllCustomerAccountsResponse[] accountsArray = response.extract().as(GetAllCustomerAccountsResponse[].class);
        return Arrays.asList(accountsArray);
    }

    public static DepositMoneyResponse depositMoney(DepositMoneyRequest request, CreateUserRequest user) {
        DepositMoneyResponse response = new ValidatedCrudRequester<DepositMoneyResponse>
                (RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()),
                        Endpoint.ACCOUNTS_DEPOSIT,
                        ResponseSpecs.requestReturnsOK())
                .post(request);
        return response;
    }

    public static TransferMoneyResponse transferMoney(TransferMoneyRequest request, CreateUserRequest user) {
        TransferMoneyResponse response = new ValidatedCrudRequester<TransferMoneyResponse>
                (RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()),
                        Endpoint.ACCOUNTS_TRANSFER,
                        ResponseSpecs.requestReturnsOK())
                .post(request);
        return response;
    }

    public static ChangeNameResponse changeName(CreateUserRequest user, String newName) {
        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(newName)
                .build();

        ChangeNameResponse response = new ValidatedCrudRequester<ChangeNameResponse>
                (RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()),
                        Endpoint.CUSTOMER_PROFILE,
                        ResponseSpecs.requestReturnsOK())
                .update(changeNameRequest);
        return response;
    }

    public static void checkAccountBalance(double expectedBalance, CreateUserRequest createUserRequest, long accountId){
        List<GetAllCustomerAccountsResponse> allAccounts = getAllCustomerAccounts(createUserRequest.getUsername(), createUserRequest.getPassword());
        GetAllCustomerAccountsResponse createdAccountInList = allAccounts.stream()
                .filter(account -> account.getId() == accountId)
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(createdAccountInList, "Аккаунт не найден в списке");
        Assertions.assertEquals(expectedBalance, createdAccountInList.getBalance(),
                "Баланс аккаунта " + accountId + " не соответствует ожидаемому");
    }

    public static void checkName(CreateUserRequest user, String expectedName, String message){
        CustomerResponse response = new ValidatedCrudRequester<CustomerResponse>
                (RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()),
                        Endpoint.CUSTOMER_PROFILE_GET,
                        ResponseSpecs.requestReturnsOK())
                .get();

        Assertions.assertEquals(expectedName, response.getName(), message);
    }

    public List<CreateAccountResponse> getAllAccounts(){
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUserSpec(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK()).getAll(CreateAccountResponse[].class);

    }

    public TransferMoneyResponse transferWithFraudCheck(Long senderAccountId, Long receiverAccountId, double amount) {
        return StepLogger.log("User " + username + " transfers " + amount + " to " + receiverAccountId + " with fraud check", () -> {
            TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                    .senderAccountId(senderAccountId)
                    .receiverAccountId(receiverAccountId)
                    .amount(amount)
                    .description("Test transfer with fraud check")
                    .build();

            return new ValidatedCrudRequester<TransferMoneyResponse>(
                    RequestSpecs.authAsUserSpec(username, password),
                    Endpoint.TRANSFER_WITH_FRAUD_CHECK,
                    ResponseSpecs.requestReturnsOK()).post(transferRequest);
        });
    }

}
