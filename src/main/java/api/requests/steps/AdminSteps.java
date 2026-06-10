package api.requests.steps;
import api.generators.RandomModelGenerator;
import common.helpers.StepLogger;
import io.restassured.response.ValidatableResponse;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.GetAllUserResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.Arrays;
import java.util.List;

public class AdminSteps {

    public static CreateUserRequest createUser() {
        return StepLogger.log("Admin creates user", () -> {
            CreateUserRequest userRequest =
                    RandomModelGenerator.generate(CreateUserRequest.class);
            new ValidatedCrudRequester<CreateUserResponse>(
                    RequestSpecs.adminSpec(),
                    Endpoint.ADMIN_USER,
                    ResponseSpecs.entityWasCreated())
                    .post(userRequest);
            return userRequest;
        });
    }

    public static List<GetAllUserResponse> gelAllUsers() {
        return StepLogger.log("Admin gets all users", () -> {
            ValidatableResponse response = new CrudRequester(
                    RequestSpecs.adminSpec(),
                    Endpoint.ADMIN_USER,
                    ResponseSpecs.requestReturnsOK())
                    .get();

            GetAllUserResponse[] usersArray = response.extract().as(GetAllUserResponse[].class);
            return Arrays.asList(usersArray);
        });
    }

    public static List<CreateUserResponse> getAllUsers() {
        return StepLogger.log("Admin gets all users", () -> {
            return new ValidatedCrudRequester<CreateUserResponse>(
                    RequestSpecs.adminSpec(),
                    Endpoint.ADMIN_USER,
                    ResponseSpecs.requestReturnsOK()).getAll(CreateUserResponse[].class);
        });
    }



}
