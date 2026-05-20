package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferMoneyResponse extends BaseModel{
    private String status;
    private String message;
    private Long transactionId;
    private double amount;
    private long receiverAccountId;
    private long senderAccountId;
    private double fraudRiskScore;
    private String fraudReason;
    private boolean requiresVerification;
    private boolean requiresManualReview;
}
