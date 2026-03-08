package apiTests.iteration1;

import apiTests.BaseTest;
import models.CreateUserRequest;
import models.GetAllCustomerAccountsResponse;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;

import java.util.List;

public class CreateAccountTest extends BaseTest {

    //Positive
    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        long createdAccountId = UserSteps.createAccount(createUserRequest).getId();

        // запросить все аккаунты юзера и проверить, что созданный аккаунт там (по id)
        List<GetAllCustomerAccountsResponse> allAccounts = UserSteps.
                getAllCustomerAccounts(createUserRequest.getUsername(), createUserRequest.getPassword());

        // Находим созданный аккаунт в списке
        GetAllCustomerAccountsResponse createdAccountInList = allAccounts.stream()
                .filter(account -> account.getId() == createdAccountId)
                .findFirst()
                .orElse(null);

        softly.assertThat(createdAccountInList).isNotNull();
    }


}
