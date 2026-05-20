package common.extensions;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AccountSteps;
import api.requests.steps.AdminSteps;
import common.annotations.PrepareUsers;
import common.storage.SessionStorage;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;

public class PrepareUsersExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        PrepareUsers annotation = extensionContext.getRequiredTestMethod()
                .getAnnotation(PrepareUsers.class);

        if (annotation != null) {
            int usersCount = annotation.value();
            List<CreateUserRequest> users = new ArrayList<>();

            SessionStorage.clear();

            for (int i = 0; i < usersCount; i++) {
                CreateUserRequest user = AdminSteps.createUser();
                users.add(user);

                AccountSteps accountSteps = new AccountSteps(user.getUsername(), user.getPassword());

                CreateAccountResponse account = accountSteps.createAccount();

                SessionStorage.setUserAccount(user, account);
            }

            SessionStorage.addUsers(users);
        }
    }
}
