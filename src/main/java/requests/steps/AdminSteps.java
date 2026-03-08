package requests.steps;
import generators.RandomModelGenerator;
import io.restassured.response.ValidatableResponse;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.GetAllUserResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Arrays;
import java.util.List;

public class AdminSteps {

    public static CreateUserRequest createUser() {
        CreateUserRequest userRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);
        new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
        return userRequest;
    }

    public static List<GetAllUserResponse> gelAllUsers() {
        ValidatableResponse response = new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsOK())
                .get();

        GetAllUserResponse[] usersArray = response.extract().as(GetAllUserResponse[].class);
        return Arrays.asList(usersArray);
    }



}
