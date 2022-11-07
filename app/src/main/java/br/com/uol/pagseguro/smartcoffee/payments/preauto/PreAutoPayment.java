package br.com.uol.pagseguro.smartcoffee.payments.preauto;

import java.io.Serializable;

public class PreAutoPayment implements Serializable {

    private final int installmentNumber;
    private final String pan;
    private final String expirationDate;
    private final String cardCvv;
    private final String transactionCode;
    private final String transactionDate;

    public PreAutoPayment(
            int installmentNumber,
            String pan,
            String expirationDate,
            String cardCvv,
            String transactionCode,
            String transactionDate
    ) {
        this.installmentNumber = installmentNumber;
        this.pan = pan;
        this.expirationDate = expirationDate;
        this.cardCvv = cardCvv;
        this.transactionCode = transactionCode;
        this.transactionDate = transactionDate;
    }

    public int getInstallmentNumber() {
        return installmentNumber;
    }

    public String getPan() {
        return pan;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getCardCvv() {
        return cardCvv;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public String getTransactionDate() {
        return transactionDate;
    }
}
