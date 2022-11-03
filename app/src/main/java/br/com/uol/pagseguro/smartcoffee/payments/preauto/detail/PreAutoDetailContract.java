package br.com.uol.pagseguro.smartcoffee.payments.preauto.detail;

import com.hannesdorfmann.mosby.mvp.MvpView;

public interface PreAutoDetailContract extends MvpView {

    void showDialog(String message);

    void showTransactionSuccess();

    void dismissDialog();

    void closeActivity();
}
