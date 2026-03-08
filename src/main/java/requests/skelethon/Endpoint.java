package requests.skelethon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

@AllArgsConstructor
@Getter
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),
    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),
    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),
    ACCOUNTS_DEPOSIT(
            "/accounts/deposit",
            BaseModel.class,
            DepositMoneyResponse.class
    ),
    ACCOUNTS_TRANSFER(
            "/accounts/transfer",
            BaseModel.class,
            TransferMoneyResponse.class
    ),
    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            GetAllCustomerAccountsResponse.class
    ),
    CUSTOMER_PROFILE(
            "/customer/profile",
            BaseModel.class,
            ChangeNameResponse.class
    );




    private  final String url;
    private  final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
