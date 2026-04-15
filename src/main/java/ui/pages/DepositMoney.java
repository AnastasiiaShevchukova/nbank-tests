package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

@Getter
public class DepositMoney extends BasePage<DepositMoney>{

    private SelenideElement selectAccount = $(".account-selector");
    private SelenideElement enterAmountField = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement depositButton = $$("button").findBy(text("Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositMoney depositMoneyToAccount(long accountId, String depositAmount){
        selectAccount.selectOption("ACC" + accountId + " (Balance: $0.00)");
        enterAmountField.sendKeys(depositAmount);
        depositButton.shouldHave(exactText("💵 Deposit")).click();
        return this;
    }
}
