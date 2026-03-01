package apiTests.iteration1;

import apiTests.BaseTest;
import generators.RandomData;
import models.CreateUserRequest;
import models.GetAllCustomerAccountsResponse;
import models.UserRole;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.GetAllCustomerAccountsRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        //создание пользователя
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
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


        // запросить все аккаунты юзера и проверить, что созданный аккаунт там (по id)
        List<GetAllCustomerAccountsResponse> allAccounts = new GetAllCustomerAccountsRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK())
                .get()
                .extract().jsonPath().getList("", GetAllCustomerAccountsResponse.class);;

        // Находим созданный аккаунт в списке
        GetAllCustomerAccountsResponse createdAccountInList = allAccounts.stream()
                .filter(account -> account.getId() == createdAccountId)
                .findFirst()
                .orElse(null);

        softly.assertThat(createdAccountInList).isNotNull();
    }


}
