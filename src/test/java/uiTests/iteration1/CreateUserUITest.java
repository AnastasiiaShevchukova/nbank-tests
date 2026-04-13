package uiTests.iteration1;

import api.requests.steps.AdminSteps;
import com.codeborne.selenide.*;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanel;
import ui.pages.BankAlerts;
import uiTests.BaseUiTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CreateUserUITest extends BaseUiTest {


    @Test
    public void adminCanCreateUserTest(){
        // ШАГ 1: админ логинится в банке
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin);
        // ШАГ 2: админ создает юзера в банке на своей панели +
        // ШАГ 3: проверка, что алерт "✅ User created successfully!"
        // ШАГ 4: проверка, что юзер отображается на UI
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlerts.USER_CREATED_SUCCESSFULLY.getMessage())
                .getAllUsers()
                .findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldBe(Condition.visible);

        // ШАГ 5: проверка, что юзер создан на API

        CreateUserResponse createdUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst()
                .get();

        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }

    @Test
    public void adminCannotCreateUserWithInvalidDataTest() {
        // ШАГ 1: админ логинится в банке
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin);
        // ШАГ 2: админ создает юзера в банке
        // ШАГ 3: проверка, что алерт "Username must be between 3 and 15 characters"
        // ШАГ 4: проверка, что юзер НЕ отображается на UI
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlerts.USERNAME_MUST_BE_BETWEEN_3_ANS_15_CHARACTERS.getMessage())
                .getAllUsers()
                .findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldNotBe(Condition.exist);

        // ШАГ 5: проверка, что юзер НЕ создан на API
        long usersWithSameUsernameAsNewUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername()
                        .equals(newUser.getUsername())).count();

        assertThat(usersWithSameUsernameAsNewUser).isZero();
    }
}
