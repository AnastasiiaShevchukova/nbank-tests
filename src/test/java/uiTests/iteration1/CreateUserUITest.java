package uiTests.iteration1;

import api.requests.steps.AdminSteps;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import common.annotations.AdminSession;
import common.annotations.Browsers;
import org.junit.jupiter.api.Test;
import ui.elements.UserBage;
import ui.pages.AdminPanel;
import ui.pages.BankAlerts;
import uiTests.BaseUiTest;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateUserUITest extends BaseUiTest {


    @Test
    @AdminSession
    @Browsers({"chrome"})
    public void adminCanCreateUserTest(){
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера в банке на своей панели +
        // ШАГ 3: проверка, что алерт "✅ User created successfully!"
        // ШАГ 4: проверка, что юзер отображается на UI
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        UserBage newUserBage = new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlerts.USER_CREATED_SUCCESSFULLY.getMessage())
                .findUserByUsername(newUser.getUsername());
        assertThat(newUserBage).as("UserBage should exist on Dashboard after user creation").isNotNull();
        // ШАГ 5: проверка, что юзер создан на API

        CreateUserResponse createdUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst()
                .get();

        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }

    @Test
    @AdminSession
    @Browsers({"chrome"})
    public void adminCannotCreateUserWithInvalidDataTest() {
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера в банке
        // ШАГ 3: проверка, что алерт "Username must be between 3 and 15 characters"
        // ШАГ 4: проверка, что юзер НЕ отображается на UI
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        assertTrue(new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlerts.USERNAME_MUST_BE_BETWEEN_3_ANS_15_CHARACTERS.getMessage())
                .getAllUsers()
                .stream().noneMatch(userBage -> userBage.getUsername().equals(newUser.getUsername())));

        // ШАГ 5: проверка, что юзер НЕ создан на API
        long usersWithSameUsernameAsNewUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername()
                        .equals(newUser.getUsername())).count();

        assertThat(usersWithSameUsernameAsNewUser).isZero();
    }
}
