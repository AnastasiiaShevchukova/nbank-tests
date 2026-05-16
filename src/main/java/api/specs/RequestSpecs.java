package api.specs;

import api.configs.Config;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import api.models.LoginUserRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestSpecs {

    private static final ThreadLocal<String> currentBaseUrl = new ThreadLocal<>();

    private static Map<String, String> authHeaders = new HashMap<>(Map.of("admin", "Basic YWRtaW46YWRtaW4="));

    private RequestSpecs() {
    }

    public static void setBaseUrl(String baseUrl) {
        currentBaseUrl.set(baseUrl);
    }

    public static void clearBaseUrl() {
        currentBaseUrl.remove();
    }

    //Дефолтная спека
    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()))
                .setBaseUri((currentBaseUrl.get() != null ? currentBaseUrl.get() : Config.getProperty("apiBaseUrl"))
                        + Config.getProperty("apiVersion"));
    }

    //Не авторизованный юзер
    public static RequestSpecification unauthSpec() {
        return defaultRequestBuilder().build();
    }

    // Admin
    public static RequestSpecification adminSpec() {
        return defaultRequestBuilder()
                .addHeader("Authorization", authHeaders.get("admin"))
                .build();
    }

    // Авторизованный юзер
    public static RequestSpecification authAsUserSpec(String username, String password) {
        return defaultRequestBuilder()
                .addHeader("Authorization", getUserAuthHeader(username, password))
                .build();
    }

    public static String getUserAuthHeader(String username, String password) {
        String userAuthHeader;
        if (!authHeaders.containsKey(username)) {
            userAuthHeader = new CrudRequester(
                    RequestSpecs.unauthSpec(),
                    Endpoint.LOGIN,
                    ResponseSpecs.requestReturnsOK())
                    .post(LoginUserRequest.builder().username(username).password(password).build())
                    .extract()
                    .header("Authorization");

            authHeaders.put(username, userAuthHeader);
        } else {
            userAuthHeader = authHeaders.get(username);
        }
        return userAuthHeader;
    }
}
