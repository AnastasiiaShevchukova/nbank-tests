package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class TransferMoney extends BasePage<TransferMoney>{

    private SelenideElement selectYourAccount = $(".account-selector");
    private SelenideElement enterReceipentNameField = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    private SelenideElement enterReceipentAccountNumberField = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private SelenideElement enterAmountMoneyField = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement confirmDetailsCheckbox = $("#confirmCheck");
    private SelenideElement sendTransferButton = $$("button").findBy(text("Send Transfer"));

    @Override
    public String url() {
        return "/transfer";
    }

    public TransferMoney transferMoneyFromAccountToAccount(long yourAccount, String receipentAccountNumber, String transferAmountMoney){
        selectYourAccount.selectOptionByValue(String.valueOf(yourAccount));
        enterReceipentNameField.sendKeys("To me");
        enterReceipentAccountNumberField.sendKeys(receipentAccountNumber);
        enterAmountMoneyField.sendKeys(transferAmountMoney);
        confirmDetailsCheckbox.click();
        sendTransferButton.shouldHave(exactText("🚀 Send Transfer")).click();
        return this;
    }
}
