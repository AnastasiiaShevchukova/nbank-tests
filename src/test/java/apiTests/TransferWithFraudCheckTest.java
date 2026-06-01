package apiTests;

import api.generators.FraudCheckTestData;
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
import org.junit.jupiter.api.Disabled;
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
            status = FraudCheckTestData.STATUS_SUCCESS,
            decision = FraudCheckTestData.DECISION_APPROVED,
            riskScore = FraudCheckTestData.FRAUD_RISK_SCORE_LOW,
            reason = FraudCheckTestData.FRAUD_REASON_LOW_RISK,
            requiresManualReview = FraudCheckTestData.REQUIRES_MANUAL_REVIEW_FALSE,
            additionalVerificationRequired = FraudCheckTestData.REQUIRES_VERIFICATION_FALSE
    )
    @PrepareUsers(2)
    @Disabled
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

        long senderId = account1.getId().longValue();
        long receiverId = account2.getId().longValue();

        TransferMoneyResponse expectedResponse = FraudCheckTestData
                .expectedApprovedTransfer(senderId, receiverId, transferAmount).build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }
}
