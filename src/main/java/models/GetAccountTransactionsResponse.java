package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetAccountTransactionsResponse extends BaseModel{
    private Long id;
    private double amount;
    private String type;
    private String timestamp;
    private Long relatedAccountId;
}
