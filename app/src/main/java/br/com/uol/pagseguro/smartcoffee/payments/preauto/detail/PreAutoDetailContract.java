package br.com.uol.pagseguro.smartcoffee.payments.preauto.detail;

import com.hannesdorfmann.mosby.mvp.MvpView;

public interface PreAutoDetailContract extends MvpView {

    void showMessage(String message);

    void showTransactionSuccess();

    void showError(String error);

    void dismissDialog();

    void closeActivity();

}
