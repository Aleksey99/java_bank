package entities;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Operation {
    private String date;
    private Currency fromCurrency;
    private String fromUUID;
    private String toUUID;
    private BigDecimal amount;
    private BigDecimal amountBefore;
    private BigDecimal amountAfter;

    public Operation(Currency fromCurrency, String fromUUID, String toUUID,
                     BigDecimal amount, BigDecimal amountBefore, BigDecimal amountAfter) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dt = new Date();
        this.date = format.format(dt);
        this.fromCurrency = fromCurrency;
        this.fromUUID = fromUUID;
        this.toUUID = toUUID;
        this.amount = amount;
        this.amountBefore = amountBefore;
        this.amountAfter = amountAfter;
    }

    public Operation(String date, String fromCurrency, String fromUUID, String toUUID,
                     String amount, String amountBefore, String amountAfter) {
        this.date = date;
        this.fromCurrency = Currency.valueOf(fromCurrency);
        this.fromUUID = fromUUID;
        this.toUUID = toUUID;
        this.amount = new BigDecimal(amount);
        this.amountBefore = new BigDecimal(amountBefore);
        this.amountAfter = new BigDecimal(amountAfter);
    }

    public String getFromUUID() {
        return fromUUID;
    }

    public String toStrList() {
        String format = "'%s', '%s', '%s', '%s', '%s', '%s', '%s'";
        return String.format(format, date, fromCurrency, fromUUID, toUUID, amount, amountBefore, amountAfter);
    }

    public String getInfo() {
        String format = "Date: %s\naccCode: %s\nSource account: %s\nDestination account: %s\nAmount: %s\n" +
                "Amount of account before transfer: %s\nAmount of account after transfer: %s";
        return String.format(format, date, fromCurrency, fromUUID, toUUID, amount, amountBefore, amountAfter);
    }
}
