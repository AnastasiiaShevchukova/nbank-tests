package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlerts {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_ANS_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    SUCCESSFULLY_DEPOSITED_MONEY_TO_ACCOUNT("✅ Successfully deposited $%s to account ACC%s!"),
    PLEASE_ENTER_A_VALID_AMOUNT("❌ Please enter a valid amount."),
    PLEASE_DEPOSIT_LESS_OR_EQUAL_TO_5000("❌ Please deposit less or equal to 5000$."),
    SUCCESSFULLY_TRANSFERRED_MONEY_TO_ACCOUNT("✅ Successfully transferred $%s to account ACC%s!"),
    ERROR_TRANSFER_AMOUNT_MUST_BE_AT_LEAST_01("❌ Error: Transfer amount must be at least 0.01"),
    ERROR_TRANSFER_AMOUNT_CANNOT_EXCEED_10000("❌ Error: Transfer amount cannot exceed 10000"),
    ERROR_INVALID_TRANSFER_INSUFFICIENT_FUNDS_OR_INVALID_ACCOUNTS("❌ Error: Invalid transfer: insufficient funds or invalid accounts"),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    PLEASE_ENTER_A_VALID_NAME("❌ Please enter a valid name."),
    NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY("Name must contain two words with letters only");


    private final String message;

    BankAlerts(String message) {
        this.message=message;
    };
}
