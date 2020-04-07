package entities;

import java.math.BigDecimal;

public class Account {
    private String uuid;
    private BigDecimal amount;
    private Currency accCode;

    public Account(String uuid, BigDecimal amount, Currency accCode) {
        this.uuid = uuid;
        this.amount = amount;
        this.accCode = accCode;
    }

    public String getUUID() {
        return uuid;
    }

    public Currency getAccCode() {
        return accCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getInfo() {
        String format = "UUID: %s\nAmount: %s\nCurrency: %s";
        return String.format(format, uuid, amount, accCode);
    }

    public static BigDecimal convert(BigDecimal sum, Currency from, Currency to) {
        BigDecimal koef = BigDecimal.valueOf(from.getRUBperUnit() / to.getRUBperUnit());
        return sum.multiply(koef);
    }
}
