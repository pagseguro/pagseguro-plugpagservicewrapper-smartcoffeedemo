package br.com.uol.pagseguro.smartcoffee.payments.credit;

import com.hannesdorfmann.mosby.mvp.MvpView;

public interface CreditPaymentContract extends MvpView {

    void showTransactionSuccess();

    void showError(String message);

    void showMessage(String message);

    void showLoading(boolean show);

    void writeToFile(String transactionCode, String transactionId);

    void disposeDialog();

    void showActivationDialog();

    void showAuthProgress(String message);
}
