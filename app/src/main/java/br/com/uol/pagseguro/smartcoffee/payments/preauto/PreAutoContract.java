package br.com.uol.pagseguro.smartcoffee.payments.preauto;

import com.hannesdorfmann.mosby.mvp.MvpView;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;

public interface PreAutoContract extends MvpView {

    void showTransactionSuccess();

    void showMessage(String message);

    void dismissDialog();

    void showLoading(boolean show);

    void writeToFile(String transactionCode, String transactionId);

    void showActivationDialog();

    void showAuthProgress(String message);

    void showDialogValuePreAuto(
        DismissListenerEffectivate onDismissListener,
        PlugPagTransactionResult plugPagTransactionResult
    );

    void showPreAutoDetail(
        PlugPagTransactionResult plugPagTransactionResult,
        Boolean isPreAutoCancel
    );

}