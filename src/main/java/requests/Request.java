package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

public abstract class Request<T extends BaseModel> {

    protected RequestSpecification requestSpecification;

    protected ResponseSpecification responseSpecification;

    public Request(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
    }

    //public abstract ValidatableResponse post(T model);

    public ValidatableResponse post(T model) {
        throw new UnsupportedOperationException(
                "POST method not supported for " + this.getClass().getSimpleName()
        );
    }

    public ValidatableResponse get() {
        throw new UnsupportedOperationException(
                "GET method not supported for " + this.getClass().getSimpleName()
        );
    }

    public ValidatableResponse get(T model) {
        throw new UnsupportedOperationException(
                "GET with params not supported for " + this.getClass().getSimpleName()
        );
    }

    public ValidatableResponse put(T model) {
        throw new UnsupportedOperationException(
                "PUT method not supported for " + this.getClass().getSimpleName()
        );
    }

    public ValidatableResponse delete() {
        throw new UnsupportedOperationException(
                "DELETE method not supported for " + this.getClass().getSimpleName()
        );
    }

    public ValidatableResponse delete(T model) {
        throw new UnsupportedOperationException(
                "DELETE with params not supported for " + this.getClass().getSimpleName()
        );
    }
}
