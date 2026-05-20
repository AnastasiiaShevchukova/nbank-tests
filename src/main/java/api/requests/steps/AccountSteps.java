package api.requests.steps;

import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.helpers.StepLogger;

public class AccountSteps {

    private String username;
    private String password;

    public AccountSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public CreateAccountResponse createAccount() {
        return StepLogger.log("User " + username + " creates account", () -> {
            return new ValidatedCrudRequester<CreateAccountResponse>(
                    RequestSpecs.authAsUserSpec(username, password),
                    Endpoint.ACCOUNTS,
                    ResponseSpecs.entityWasCreated()).post(null);
        });
    }

    public DepositMoneyResponse depositToAccount(Long accountId, double amount) {
        return StepLogger.log("User " + username + " deposits " + amount + " to account " + accountId, () -> {
            DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                    .accountId(accountId)
                    .amount(amount)
                    .description("Test deposit")
                    .build();

            return new ValidatedCrudRequester<DepositMoneyResponse>(
                    RequestSpecs.authAsUserSpec(username, password),
                    Endpoint.ACCOUNTS_DEPOSIT,
                    ResponseSpecs.requestReturnsOK()).post(depositRequest);
        });
    }

}
