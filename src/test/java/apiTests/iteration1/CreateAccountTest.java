package apiTests.iteration1;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.CreateAccountResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.DataBaseSteps;
import api.specs.ResponseSpecs;
import api.specs.RequestSpecs;
import apiTests.BaseTest;
import api.models.CreateUserRequest;
import common.annotations.APIBackend;
import common.annotations.APIVersion;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;

@APIVersion(APIBackend.DATABASE_FIX)
public class CreateAccountTest extends BaseTest {

    //Positive
    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse createAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>
                (RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                        Endpoint.ACCOUNTS,
                        ResponseSpecs.entityWasCreated())
                .post(null);

        // Проверка через БД
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        DaoAndModelAssertions.assertThat(createAccountResponse, accountDao).match();
    }


}
