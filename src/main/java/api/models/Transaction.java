package api.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transaction {

    private long id;
    private double amount;
    private String type;
    private String timestamp;
    private long relatedAccountId;
}
