package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountResponse extends BaseModel{
    private Long id;
    private String accountNumber;
    private double balance;
    private List<String> transactions;

}
