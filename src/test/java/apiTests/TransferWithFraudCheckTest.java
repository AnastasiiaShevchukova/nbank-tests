package apiTests;

import api.models.*;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AccountSteps;
import api.requests.steps.UserSteps;
import common.annotations.PrepareUsers;
import common.extensions.FraudCheckWireMockExtension;
import common.extensions.TimingExtension;
import common.annotations.FraudCheckMock;
import common.storage.SessionStorage;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferWithFraudCheckTest extends BaseTest{

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
    }

    @Test
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    @PrepareUsers(2)
    public void testTransferWithFraudCheck() {
        UserSteps userSteps1 = SessionStorage.getSteps(1);
        CreateAccountResponse account1 = SessionStorage.getUserAccount(1);
        CreateAccountResponse account2 = SessionStorage.getUserAccount(2);

        AccountSteps accountSteps1 = new AccountSteps(SessionStorage.getUser(1).getUsername(), SessionStorage.getUser(1).getPassword());
        account1 = accountSteps1.createAccount();
        double depositAmount = Math.random() * 4999.9 + 0.1;
        accountSteps1.depositToAccount(account1.getId(), depositAmount);


        // Шаги теста
        double transferAmount = Math.random() * (depositAmount - 0.1) + 0.1;
        // Попытка перевода денег с проверкой на фрод
        TransferMoneyResponse transferResponse = userSteps1.transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                transferAmount
        );

        softly.assertThat(transferResponse).isNotNull();
        TransferMoneyResponse expectedResponse = TransferMoneyResponse.builder()
                .status("APPROVED")
                .message("Transfer approved and processed immediately")
                .amount(transferAmount)
                .senderAccountId(account1.getId())
                .receiverAccountId(account2.getId())
                .fraudRiskScore(0.2)
                .fraudReason("Low risk transaction")
                .requiresManualReview(false)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }
}
