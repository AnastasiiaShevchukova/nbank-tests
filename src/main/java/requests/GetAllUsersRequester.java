package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.GetAllUserRequest;

import static io.restassured.RestAssured.given;

public class GetAllUsersRequester extends Request<GetAllUserRequest> {

    public GetAllUsersRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(GetAllUserRequest model) {
        return given()
                .spec(requestSpecification)
                .get("/api/v1/admin/users")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
