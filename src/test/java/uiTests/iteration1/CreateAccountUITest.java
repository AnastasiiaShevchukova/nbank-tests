package uiTests.iteration1;

import api.requests.steps.UserSteps;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import ui.pages.BankAlerts;
import ui.pages.UserDashboard;
import uiTests.BaseUiTest;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountUITest extends BaseUiTest {

    @Test
    public void userCanCreateAccountTest() {
        // ШАГИ по настройке окружения
        // ШАГ 1 админ логинится в банке
        // ШАГ 2 админ создает юзера
        // ШАГ 3 юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        // ШАГИ теста
        // шаг 4 - юзер создает аккаунт
        // шаг 5 - проверка, что аккаунт создался на ЮАЙ
        // шаг 6 - Проверка, что аккаунт создался на АПИ
        new UserDashboard().open().createNewAccount();

        List<CreateAccountResponse> createdAccounts = new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccounts();
        assertThat(createdAccounts).hasSize(1);

        new UserDashboard().checkAlertMessageAndAccept
                (BankAlerts.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());
        assertThat(createdAccounts.getFirst().getBalance()).isZero();

    }
}
