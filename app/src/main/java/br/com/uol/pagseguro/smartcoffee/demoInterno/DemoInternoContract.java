package br.com.uol.pagseguro.smartcoffee.demoInterno;

import com.hannesdorfmann.mosby.mvp.MvpView;

interface DemoInternoContract extends MvpView{

    void showTransactionSuccess();

    void showError(String message);

    void showMessage(String message);

    void showLoading(boolean show);

    void writeToFile(String transactionCode, String transactionId);

    void showAbortedSuccessfully();

    void disposeDialog();

    void showActivationDialog();

    void showAuthProgress(String message);

    void setPaymentValue(String value);

}
