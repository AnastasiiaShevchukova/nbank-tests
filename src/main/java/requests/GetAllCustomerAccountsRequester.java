package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.GetAllCustomerAccountsRequest;

import static io.restassured.RestAssured.given;

public class GetAllCustomerAccountsRequester extends Request<GetAllCustomerAccountsRequest> {
    public GetAllCustomerAccountsRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse get() {
        return given()
                .spec(requestSpecification)
                .basePath("/api/v1/customer/accounts")
                .get()
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
