package api.generators;

import api.models.TransferMoneyResponse;


public class FraudCheckTestData {
    // Статусы
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String DECISION_APPROVED = "APPROVED";
    public static final String TRANSFER_STATUS_APPROVED = "APPROVED";

    // Сообщения
    public static final String TRANSFER_MESSAGE_APPROVED = "Transfer approved and processed immediately";
    public static final String FRAUD_REASON_LOW_RISK = "Low risk transaction";

    // Значения
    public static final double FRAUD_RISK_SCORE_LOW = 0.2;
    public static final boolean REQUIRES_MANUAL_REVIEW_FALSE = false;
    public static final boolean REQUIRES_VERIFICATION_FALSE = false;

    public static TransferMoneyResponse.TransferMoneyResponseBuilder expectedApprovedTransfer(
            long senderAccountId,
            long receiverAccountId,
            double amount) {

        return TransferMoneyResponse.builder()
                .status(DECISION_APPROVED)
                .message(TRANSFER_MESSAGE_APPROVED)
                .amount(amount)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .fraudRiskScore(FRAUD_RISK_SCORE_LOW)
                .fraudReason(FRAUD_REASON_LOW_RISK)
                .requiresManualReview(REQUIRES_MANUAL_REVIEW_FALSE)
                .requiresVerification(REQUIRES_VERIFICATION_FALSE);
    }

    public static TransferMoneyResponse.TransferMoneyResponseBuilder expectedFailTransfer(
            String errorMsg) {

        return TransferMoneyResponse.builder()
                .status(null)
                .message(errorMsg)
                .amount(0)
                .senderAccountId(0)
                .receiverAccountId(0)
                .fraudRiskScore(0.0)
                .fraudReason(null)
                .requiresManualReview(REQUIRES_MANUAL_REVIEW_FALSE)
                .requiresVerification(REQUIRES_VERIFICATION_FALSE);
    }
}
