package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class EditProfile extends BasePage<EditProfile>{

    private SelenideElement enterNewNameField = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangesButton = $$("button").findBy(text("Save Changes"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfile updateUserName(String newName){
        enterNewNameField.sendKeys(newName);
        saveChangesButton.shouldHave(exactText("\uD83D\uDCBE Save Changes")).click();
        return this;
    }
}
