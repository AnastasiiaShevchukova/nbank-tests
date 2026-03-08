package requests.steps;

import io.restassured.response.ValidatableResponse;
import models.*;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Arrays;
import java.util.List;

public class UserSteps {

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
}
