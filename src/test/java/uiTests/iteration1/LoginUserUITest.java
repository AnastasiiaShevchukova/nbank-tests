package uiTests.iteration1;

import com.codeborne.selenide.Condition;
import api.models.CreateUserRequest;
import common.annotations.APIBackend;
import common.annotations.APIVersion;
import common.annotations.Browsers;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import ui.pages.AdminPanel;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;
import uiTests.BaseUiTest;

@APIVersion(APIBackend.DATABASE_FIX)
public class LoginUserUITest extends BaseUiTest {

    @Test
    @Browsers({"chrome"})
    public void adminCanLoginWithCorrectDataTest(){
        CreateUserRequest admin = CreateUserRequest.getAdmin();

        new LoginPage().open().login(admin.getUsername(), admin.getPassword())
                .getPage(AdminPanel.class)
                .getAdminPanelText()
                .shouldBe(Condition.visible);
    }

    @Test
    @Browsers({"chrome"})
    public void userCanLoginWithCorrectData(){
        // create user
        CreateUserRequest user = AdminSteps.createUser();

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class)
                .getWelcomeText()
                .shouldBe(Condition.visible)
                .shouldBe(Condition.text("Welcome, noname!"));
    }
}
