package uiTests.iteration1;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.CreateAccountResponse;
import api.requests.steps.DataBaseSteps;
import common.annotations.APIBackend;
import common.annotations.APIVersion;
import common.annotations.Browsers;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlerts;
import ui.pages.UserDashboard;
import uiTests.BaseUiTest;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@APIVersion(APIBackend.DATABASE_FIX)
public class CreateAccountUITest extends BaseUiTest {

    @Test
    @UserSession()
    @Browsers({"chrome"})
    public void userCanCreateAccountTest() {
        // ШАГИ по настройке окружения
        // ШАГ 1 админ логинится в банке
        // ШАГ 2 админ создает юзера
        // ШАГ 3 юзер логинится в банке

        // ШАГИ теста
        // шаг 4 - юзер создает аккаунт
        // шаг 5 - проверка, что аккаунт создался на ЮАЙ
        // шаг 6 - Проверка, что аккаунт создался на АПИ
        new UserDashboard().open().createNewAccount();

        List<CreateAccountResponse> createdAccounts = SessionStorage.getSteps().getAllAccounts();
        assertThat(createdAccounts).hasSize(1);

        new UserDashboard().checkAlertMessageAndAccept
                (BankAlerts.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());
        assertThat(createdAccounts.getFirst().getBalance()).isZero();

        // Проверка через БД
        //AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createdAccounts.getFirst().getAccountNumber());
        //DaoAndModelAssertions.assertThat(createdAccounts.getFirst(), accountDao).match();

    }
}
