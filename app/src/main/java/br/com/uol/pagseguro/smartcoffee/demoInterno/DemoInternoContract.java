package br.com.uol.pagseguro.smartcoffee.demoInterno;

import com.hannesdorfmann.mosby.mvp.MvpView;

interface DemoInternoContract extends MvpView{

    void showTransactionSuccess();

    void showTransactionDialog(String message);

    void showLoading(boolean show);

    void writeToFile(String transactionCode, String transactionId);

    void disposeDialog();

    void setPaymentValue(String value);

}
